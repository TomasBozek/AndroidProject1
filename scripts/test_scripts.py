#!/usr/bin/env python3
"""
Smoke tests for the scaffolding scripts.

    python3 scripts/test_scripts.py
    python3 scripts/test_scripts.py -v ScaffoldingTest.test_delete_feature_restores_every_registration

Each test copies the repository's sources into a temporary directory and runs the scripts there as
subprocesses, so nothing touches the working tree. They check the registrations and the generated
text — not that the result compiles; `./gradlew build` is still the real gate.

Plain `unittest`, so there is no dependency to install.
"""

from __future__ import annotations

import shutil
import subprocess
import sys
import tempfile
import tomllib
import unittest
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parent.parent
BASE_PATH = "com/example/androidproject1"

# Build output and IDE state; everything else is copied so the scripts see a realistic repo.
IGNORED = shutil.ignore_patterns("build", ".gradle", ".git", ".idea", ".kotlin", "__pycache__", ".DS_Store")


class ScaffoldingTest(unittest.TestCase):

    def setUp(self) -> None:
        self._temporary = tempfile.TemporaryDirectory()
        self.addCleanup(self._temporary.cleanup)
        self.repo = Path(self._temporary.name) / "repo"
        shutil.copytree(REPO_ROOT, self.repo, ignore=IGNORED)

    # -- helpers ------------------------------------------------------------------------------

    def run_script(self, name: str, *args: str, expect_success: bool = True) -> subprocess.CompletedProcess:
        result = subprocess.run(
            [sys.executable, str(self.repo / "scripts" / name), *args],
            capture_output=True,
            text=True,
        )
        if expect_success and result.returncode != 0:
            self.fail(f"{name} {' '.join(args)} failed:\n{result.stdout}\n{result.stderr}")
        return result

    def read(self, relative: str) -> str:
        return (self.repo / relative).read_text()

    def presentation(self, feature: str) -> Path:
        return self.repo / f"feature/{feature}/presentation/src/main/kotlin/{BASE_PATH}/feature/{feature}/presentation"

    def assert_doctor_passes(self) -> None:
        result = self.run_script("doctor.py", expect_success=False)
        self.assertEqual(0, result.returncode, f"doctor.py reported problems:\n{result.stdout}")

    def tree_snapshot(self) -> set[tuple[str, int]]:
        return {
            (str(p.relative_to(self.repo)), p.stat().st_size)
            for p in self.repo.rglob("*")
            if p.is_file() and "__pycache__" not in p.parts
        }

    # -- tests --------------------------------------------------------------------------------

    def test_doctor_passes_on_a_clean_checkout(self) -> None:
        self.assert_doctor_passes()

    def test_create_feature_registers_every_module(self) -> None:
        self.run_script("create_feature.py", "userProfile")

        for layer in ("domain", "gateway", "data", "presentation", "di"):
            self.assertTrue((self.repo / f"feature/userprofile/{layer}/build.gradle.kts").is_file(), layer)

        self.assertIn('includeFeatureModule(\n    "userprofile",', self.read("settings.gradle.kts"))
        self.assertIn("api(projects.feature.userprofile.di)", self.read("core/di/build.gradle.kts"))

        koin = self.read(f"core/di/src/main/kotlin/{BASE_PATH}/core/di/Koin.kt")
        self.assertIn("UserProfileModule.module,", koin)
        self.assertIn("import com.example.androidproject1.feature.userprofile.di.UserProfileModule", koin)

        nav_host = self.read(f"app/src/main/java/{BASE_PATH}/AppNavHost.kt")
        self.assertIn("userprofileDestination(navController = navController)", nav_host)
        self.assertIn("import com.example.androidproject1.feature.userprofile.presentation.userprofileDestination", nav_host)

        self.assert_doctor_passes()

    def test_create_feature_renames_string_resources(self) -> None:
        """The template's `template_title` must not survive into a generated feature."""
        self.run_script("create_feature.py", "userProfile")

        strings = self.read("feature/userprofile/presentation/src/main/res/values/strings.xml")
        self.assertIn('name="user_profile_title"', strings)
        self.assertNotIn("template_", strings)

        screen = (self.presentation("userprofile") / "UserProfileScreen.kt").read_text()
        self.assertIn("R.string.user_profile_title", screen)
        self.assertNotIn("template_", screen)

    def test_create_feature_with_partial_layers(self) -> None:
        self.run_script("create_feature.py", "userProfile", "--layers", "presentation,di")

        self.assertFalse((self.repo / "feature/userprofile/domain").exists())
        build_file = self.read("feature/userprofile/di/build.gradle.kts")
        self.assertNotIn("projects.feature.userprofile.domain", build_file)
        self.assertIn("projects.feature.userprofile.presentation", build_file)
        self.assert_doctor_passes()

    def test_adding_a_layer_later_updates_the_settings_block(self) -> None:
        """`--layers X --force` on an existing feature must extend its includeFeatureModule block."""
        self.run_script("create_feature.py", "userProfile", "--layers", "presentation,di")
        self.run_script("create_feature.py", "userProfile", "--layers", "domain", "--force")

        settings = self.read("settings.gradle.kts")
        block = settings[settings.index('includeFeatureModule(\n    "userprofile",'):]
        block = block[: block.index(")")]
        self.assertIn("ModuleSuffix.Domain,", block)
        self.assertIn("ModuleSuffix.Presentation,", block)
        self.assertIn("ModuleSuffix.Di,", block)
        # Canonical order, not the order the layers were generated in.
        self.assertLess(block.index("Domain"), block.index("Presentation"))

        self.assert_doctor_passes()

    def test_create_screen_generates_a_usable_unit(self) -> None:
        self.run_script("create_feature.py", "userProfile")
        self.run_script("create_screen.py", "userprofile", "UserProfileList")

        directory = self.presentation("userprofile")
        for suffix in ("Destination", "Screen", "State", "Event", "Navigation", "ViewModel"):
            self.assertTrue((directory / f"UserProfileList{suffix}.kt").is_file(), suffix)

        # The bug this test exists for: the screen used to reference a string it did not bring.
        strings = self.read("feature/userprofile/presentation/src/main/res/values/strings.xml")
        self.assertIn('name="user_profile_list_title"', strings)
        self.assertIn("R.string.user_profile_list_title", (directory / "UserProfileListScreen.kt").read_text())

        module = self.read(f"feature/userprofile/di/src/main/kotlin/{BASE_PATH}/feature/userprofile/di/UserProfileModule.kt")
        self.assertIn("viewModelOf(::UserProfileListViewModel)", module)

        nav_host = self.read(f"app/src/main/java/{BASE_PATH}/AppNavHost.kt")
        self.assertIn("userProfileListDestination(navController = navController)", nav_host)

        self.assert_doctor_passes()

    def test_create_screen_in_a_sub_package_imports_r(self) -> None:
        """R lives in the module namespace, which a sub-package is no longer part of."""
        self.run_script("create_feature.py", "userProfile")
        self.run_script("create_screen.py", "userprofile", "UserProfileDetail", "--sub", "detail")

        screen = (self.presentation("userprofile") / "detail/UserProfileDetailScreen.kt").read_text()
        self.assertIn("package com.example.androidproject1.feature.userprofile.presentation.detail", screen)
        self.assertIn("import com.example.androidproject1.feature.userprofile.presentation.R", screen)

        module = self.read(f"feature/userprofile/di/src/main/kotlin/{BASE_PATH}/feature/userprofile/di/UserProfileModule.kt")
        self.assertIn(
            "import com.example.androidproject1.feature.userprofile.presentation.detail.UserProfileDetailViewModel",
            module,
        )
        self.assert_doctor_passes()

    def test_create_screen_twice_does_not_collide_on_strings(self) -> None:
        self.run_script("create_feature.py", "userProfile")
        self.run_script("create_screen.py", "userprofile", "UserProfileList")
        self.run_script("create_screen.py", "userprofile", "UserProfileDetail")

        strings = self.read("feature/userprofile/presentation/src/main/res/values/strings.xml")
        self.assertIn('name="user_profile_list_title"', strings)
        self.assertIn('name="user_profile_detail_title"', strings)
        self.assert_doctor_passes()

    def test_create_datasource_puts_each_half_in_the_right_layer(self) -> None:
        self.run_script("create_feature.py", "userProfile")
        self.run_script("create_datasource.py", "userprofile", "LocalUserProfile", "--repository")

        gateway = self.repo / f"feature/userprofile/gateway/src/main/kotlin/{BASE_PATH}/feature/userprofile/gateway"
        data = self.repo / f"feature/userprofile/data/src/main/kotlin/{BASE_PATH}/feature/userprofile/data"
        domain = self.repo / f"feature/userprofile/domain/src/main/kotlin/{BASE_PATH}/feature/userprofile/domain"

        # The interface belongs to gateway and the implementation to data, not the reverse.
        self.assertTrue((gateway / "LocalUserProfileDataSource.kt").is_file())
        self.assertTrue((data / "DefaultLocalUserProfileDataSource.kt").is_file())
        self.assertTrue((domain / "UserProfileRepository.kt").is_file())
        self.assertTrue((gateway / "DefaultUserProfileRepository.kt").is_file())

        module = self.read(f"feature/userprofile/di/src/main/kotlin/{BASE_PATH}/feature/userprofile/di/UserProfileModule.kt")
        self.assertIn("singleOf(::DefaultLocalUserProfileDataSource) bind LocalUserProfileDataSource::class", module)
        self.assertIn("singleOf(::DefaultUserProfileRepository) bind UserProfileRepository::class", module)
        self.assertIn("import org.koin.core.module.dsl.singleOf", module)
        self.assertIn("import org.koin.dsl.bind", module)

        self.assert_doctor_passes()

    def test_delete_feature_restores_every_registration(self) -> None:
        watched = [
            "settings.gradle.kts",
            "core/di/build.gradle.kts",
            f"core/di/src/main/kotlin/{BASE_PATH}/core/di/Koin.kt",
            f"app/src/main/java/{BASE_PATH}/AppNavHost.kt",
        ]
        before = {path: self.read(path) for path in watched}

        self.run_script("create_feature.py", "userProfile")
        self.run_script("create_screen.py", "userprofile", "UserProfileList")
        self.run_script("create_datasource.py", "userprofile", "LocalUserProfile", "--repository")
        self.run_script("delete_feature.py", "userProfile")

        self.assertFalse((self.repo / "feature/userprofile").exists())
        for path in watched:
            self.assertEqual(before[path], self.read(path), f"{path} was not restored")

        self.assert_doctor_passes()

    def test_delete_feature_refuses_the_template(self) -> None:
        result = self.run_script("delete_feature.py", "template", expect_success=False)
        self.assertNotEqual(0, result.returncode)
        self.assertTrue((self.repo / "feature/template").is_dir())

    def test_dry_run_writes_nothing(self) -> None:
        before = self.tree_snapshot()
        self.run_script("create_feature.py", "userProfile", "--dry-run")
        self.run_script("create_screen.py", "template", "UserProfileList", "--dry-run")
        self.assertEqual(before, self.tree_snapshot())

    def test_export_service_rewrites_the_package_and_the_catalog(self) -> None:
        target = Path(self._temporary.name) / "target"
        self.run_script("export_service.py", "--to", str(target), "--package", "com.acme.myapp", "--sync-versions")

        self.assertTrue((target / "service/core/ui/src/main/kotlin/com/acme/myapp/core/ui/component/Screen.kt").is_file())

        leftovers = [
            str(path.relative_to(target))
            for path in target.rglob("*")
            if path.is_file() and path.suffix in {".kt", ".kts", ".xml"}
            and "com.example.androidproject1" in path.read_text()
        ]
        self.assertEqual([], leftovers, "the base package survived the export")

        catalog = tomllib.loads((target / "gradle/libs.versions.toml").read_text())
        self.assertIn("kotlinx-coroutines-core", catalog["libraries"])
        self.assertIn("compose-core", catalog["bundles"])
        self.assertIn("android-library", catalog["plugins"])
        # Version refs the copied build files rely on must come along too.
        self.assertIn("coroutines", catalog["versions"])
        self.assertIn("agp", catalog["versions"])

    def test_doctor_catches_an_unregistered_view_model(self) -> None:
        self.run_script("create_feature.py", "userProfile")
        module = self.repo / f"feature/userprofile/di/src/main/kotlin/{BASE_PATH}/feature/userprofile/di/UserProfileModule.kt"
        module.write_text(module.read_text().replace("viewModelOf(::UserProfileViewModel)", ""))

        result = self.run_script("doctor.py", expect_success=False)
        self.assertNotEqual(0, result.returncode)
        self.assertIn("UserProfileViewModel", result.stdout)


if __name__ == "__main__":
    unittest.main(verbosity=2)
