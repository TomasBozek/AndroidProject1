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

# Built rather than written out: init_project.py rewrites the literal forms, so spelling them here
# would leave this file asserting against whatever the project was renamed to.
TEMPLATE_PACKAGE_WORD = "android" + "project1"
TEMPLATE_PROJECT_NAME = "Android" + "Project1"

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

        for layer in ("domain", "data", "presentation", "di"):
            self.assertTrue((self.repo / f"feature/userprofile/{layer}/build.gradle.kts").is_file(), layer)

        self.assertIn('includeFeatureModule(\n    "userprofile",', self.read("settings.gradle.kts"))
        self.assertIn("api(projects.feature.userprofile.di)", self.read("core/di/build.gradle.kts"))

        koin = self.read(f"core/di/src/main/kotlin/{BASE_PATH}/core/di/Koin.kt")
        self.assertIn("UserProfileModule.module,", koin)
        self.assertIn("import com.example.androidproject1.feature.userprofile.di.UserProfileModule", koin)

        # camelCase, the same spelling create_screen.py produces; the package stays flat.
        nav_host = self.read(f"app/src/main/kotlin/{BASE_PATH}/AppNavHost.kt")
        self.assertIn("userProfileDestination(backStack = backStack)", nav_host)
        self.assertIn("import com.example.androidproject1.feature.userprofile.presentation.userProfileDestination", nav_host)
        self.assertNotIn("userprofileDestination", nav_host)

        destination = (self.presentation("userprofile") / "UserProfileDestination.kt").read_text()
        self.assertIn("fun EntryProviderScope<NavKey>.userProfileDestination(", destination)

        # The fifth registration: CLAUDE.md's module tree, which doctor.py checks.
        self.assertIn(
            ":feature:userprofile:{domain,data,presentation,di}",
            self.read("CLAUDE.md"),
        )

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

    def test_create_feature_registers_only_the_screens_it_copied(self) -> None:
        """The di module names both template ViewModels; only one screen is copied.

        `is_copyable` skips the args screen by file name, which leaves the di module — a single
        file — importing and registering a ViewModel that does not exist. The generated feature
        did not compile.
        """
        self.run_script("create_feature.py", "userProfile")

        module = self.read(
            "feature/userprofile/di/src/main/kotlin/com/example/androidproject1"
            "/feature/userprofile/di/UserProfileModule.kt"
        )
        self.assertIn("viewModelOf(::UserProfileViewModel)", module)
        self.assertNotIn("UserProfileArgsViewModel", module)

    def test_create_feature_lists_partial_layers_in_the_module_tree(self) -> None:
        """The tree documents what is on disk; a screen-only feature must not claim five layers."""
        self.run_script("create_feature.py", "userProfile", "--layers", "presentation,di")
        self.assertIn(":feature:userprofile:{presentation,di}", self.read("CLAUDE.md"))

        self.run_script("create_feature.py", "userProfile", "--layers", "domain", "--force")
        self.assertIn(":feature:userprofile:{domain,presentation,di}", self.read("CLAUDE.md"))
        self.assert_doctor_passes()

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

        nav_host = self.read(f"app/src/main/kotlin/{BASE_PATH}/AppNavHost.kt")
        self.assertIn("userProfileListDestination(backStack = backStack)", nav_host)

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

    def test_create_datasource_puts_each_half_in_the_right_package(self) -> None:
        self.run_script("create_feature.py", "userProfile")
        self.run_script("create_datasource.py", "userprofile", "LocalUserProfile", "--repository")

        data = self.repo / f"feature/userprofile/data/src/main/kotlin/{BASE_PATH}/feature/userprofile/data"
        domain = self.repo / f"feature/userprofile/domain/src/main/kotlin/{BASE_PATH}/feature/userprofile/domain"

        # Both halves of the source sit together; the repository is one package over, and only its
        # interface — the one in domain — is visible above the data layer.
        self.assertTrue((data / "source/LocalUserProfileDataSource.kt").is_file())
        self.assertTrue((data / "source/DefaultLocalUserProfileDataSource.kt").is_file())
        self.assertTrue((domain / "UserProfileRepository.kt").is_file())
        self.assertTrue((data / "repository/DefaultUserProfileRepository.kt").is_file())

        # The repository depends on the interface, never on the implementation — doctor.py's
        # "no repository imports a data source implementation" check, asserted at the source.
        repository = (data / "repository/DefaultUserProfileRepository.kt").read_text()
        self.assertIn(
            "import com.example.androidproject1.feature.userprofile.data.source.LocalUserProfileDataSource",
            repository,
        )
        self.assertNotIn("DefaultLocalUserProfileDataSource", repository)

        module = self.read(f"feature/userprofile/di/src/main/kotlin/{BASE_PATH}/feature/userprofile/di/UserProfileModule.kt")
        self.assertIn("singleOf(::DefaultLocalUserProfileDataSource) bind LocalUserProfileDataSource::class", module)
        self.assertIn("singleOf(::DefaultUserProfileRepository) bind UserProfileRepository::class", module)
        self.assertIn("import org.koin.core.module.dsl.singleOf", module)
        self.assertIn("import org.koin.dsl.bind", module)

        # A data source touches disk, so it switches to IO itself — BaseRepository runs on the
        # caller's context, and that caller is viewModelScope.
        implementation = (data / "source/DefaultLocalUserProfileDataSource.kt").read_text()
        self.assertIn("import com.example.androidproject1.core.domain.coroutines.DispatcherProvider", implementation)
        self.assertIn("private val dispatcherProvider: DispatcherProvider,", implementation)
        self.assertIn(".flowOn(dispatcherProvider.io)", implementation)
        self.assertIn("withContext(dispatcherProvider.io)", implementation)

        self.assert_doctor_passes()

    def test_delete_feature_restores_every_registration(self) -> None:
        watched = [
            "settings.gradle.kts",
            "core/di/build.gradle.kts",
            f"core/di/src/main/kotlin/{BASE_PATH}/core/di/Koin.kt",
            f"app/src/main/kotlin/{BASE_PATH}/AppNavHost.kt",
            "CLAUDE.md",
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

    def test_create_screen_with_args_generates_the_savedstatehandle_wiring(self) -> None:
        self.run_script(
            "create_screen.py", "catalog", "ProductReview",
            "--with-args", "productId:String,rating:Int",
        )

        destination = (self.presentation("catalog") / "ProductReviewDestination.kt").read_text()
        self.assertIn(
            "data class ProductReviewDestination(val productId: String, val rating: Int) : NavKey",
            destination,
        )
        # The key goes straight into the ViewModel, not into a load() call from here. Match the
        # call, not the word: the template's comment explains why LaunchedEffect is wrong.
        self.assertIn("parametersOf(key)", destination)
        self.assertNotIn("LaunchedEffect(", destination)
        self.assertNotIn("viewModel.load(", destination)

        view_model = (self.presentation("catalog") / "ProductReviewViewModel.kt").read_text()
        self.assertIn("private val args: ProductReviewDestination", view_model)
        self.assertNotIn("SavedStateHandle", view_model)

        # The sixth registration: Koin's verify() cannot see a parametersOf argument.
        graph_test = self.read(f"app/src/test/kotlin/{BASE_PATH}/KoinGraphTest.kt")
        self.assertIn("definition<ProductReviewViewModel>(ProductReviewDestination::class)", graph_test)

    def test_create_screen_with_args_generates_a_plain_jvm_test(self) -> None:
        self.run_script("create_screen.py", "catalog", "ProductReview", "--with-args", "productId:String")

        test = (
            self.repo / f"feature/catalog/presentation/src/test/kotlin/{BASE_PATH}"
            "/feature/catalog/presentation/ProductReviewViewModelTest.kt"
        ).read_text()
        # Navigation 3 hands the key over as a plain object, so there is no Bundle to decode and
        # no Robolectric runner.
        self.assertIn("ProductReviewDestination(productId = \"example\")", test)
        self.assertNotIn("RobolectricTestRunner", test)

    def test_create_screen_with_args_defaults_to_a_single_id(self) -> None:
        self.run_script("create_screen.py", "catalog", "ProductReview", "--with-args")

        self.assertIn(
            "data class ProductReviewDestination(val id: String)",
            (self.presentation("catalog") / "ProductReviewDestination.kt").read_text(),
        )

    def test_create_screen_with_args_names_files_without_the_args_suffix(self) -> None:
        """The template class is `TemplateArgs`; the generated files must not inherit the suffix."""
        self.run_script("create_screen.py", "catalog", "ProductReview", "--with-args")

        self.assertTrue((self.presentation("catalog") / "ProductReviewViewModel.kt").is_file())
        self.assertFalse((self.presentation("catalog") / "ProductReviewArgsViewModel.kt").exists())
        self.assert_doctor_passes()

    def test_create_screen_with_args_rejects_an_unsupported_type(self) -> None:
        result = self.run_script(
            "create_screen.py", "catalog", "ProductReview", "--with-args", "productId:Uri",
            expect_success=False,
        )
        self.assertNotEqual(0, result.returncode)
        self.assertIn("not a supported", result.stdout + result.stderr)

    def test_create_screen_without_args_is_unchanged(self) -> None:
        """The plain path must not pick up the args template — `Template*.kt` matches both."""
        self.run_script("create_screen.py", "catalog", "PlainScreen")

        destination = (self.presentation("catalog") / "PlainScreenDestination.kt").read_text()
        self.assertIn("data object PlainScreenDestination", destination)
        self.assertFalse((self.presentation("catalog") / "PlainScreenArgsDestination.kt").exists())
        view_model = (self.presentation("catalog") / "PlainScreenViewModel.kt").read_text()
        self.assertNotIn("SavedStateHandle", view_model)
        self.assert_doctor_passes()


    def test_every_script_documents_itself(self) -> None:
        """--help must work and carry a worked example; the README must list every script."""
        import subprocess
        readme = (self.repo / "scripts/README.md").read_text()

        scripts = sorted(
            p for p in (self.repo / "scripts").glob("*.py")
            if p.name not in {"_common.py", "test_scripts.py"}
        )
        self.assertTrue(scripts)

        for script in scripts:
            result = subprocess.run(
                [sys.executable, str(script), "--help"], capture_output=True, text=True,
            )
            self.assertEqual(0, result.returncode, f"{script.name} --help failed:\n{result.stderr}")
            self.assertIn(
                f"scripts/{script.name}", result.stdout,
                f"{script.name} --help has no worked example",
            )
            self.assertIn(script.name, readme, f"scripts/README.md does not mention {script.name}")


    # -- install_hooks.py ---------------------------------------------------------------------

    def test_install_hooks_writes_an_executable_pre_commit_hook(self) -> None:
        import subprocess
        subprocess.run(["git", "init", "-q"], cwd=self.repo, check=True)

        self.run_script("install_hooks.py")

        hook = self.repo / ".git/hooks/pre-commit"
        self.assertTrue(hook.is_file())
        self.assertTrue(hook.stat().st_mode & 0o111, "hook is not executable")
        text = hook.read_text()
        self.assertIn("doctor.py", text)
        # test_scripts.py takes ~20s, so it runs only when the commit touches the generators.
        self.assertIn("grep -q '^scripts/'", text)

    def test_install_hooks_is_idempotent_and_uninstalls(self) -> None:
        import subprocess
        subprocess.run(["git", "init", "-q"], cwd=self.repo, check=True)

        self.run_script("install_hooks.py")
        self.run_script("install_hooks.py")
        self.assertTrue((self.repo / ".git/hooks/pre-commit").is_file())

        self.run_script("install_hooks.py", "--uninstall")
        self.assertFalse((self.repo / ".git/hooks/pre-commit").exists())

    def test_install_hooks_refuses_to_clobber_a_foreign_hook(self) -> None:
        import subprocess
        subprocess.run(["git", "init", "-q"], cwd=self.repo, check=True)
        hook = self.repo / ".git/hooks/pre-commit"
        hook.parent.mkdir(parents=True, exist_ok=True)
        hook.write_text("#!/bin/sh\necho mine\n")

        result = self.run_script("install_hooks.py", expect_success=False)

        self.assertNotEqual(0, result.returncode)
        self.assertIn("echo mine", hook.read_text(), "someone else's hook was overwritten")

    # -- slash commands -----------------------------------------------------------------------

    def test_slash_commands_reference_scripts_that_exist(self) -> None:
        import re
        commands = sorted((self.repo / ".claude/commands").glob("*.md"))
        self.assertTrue(commands, "no slash commands found")
        for command in commands:
            text = command.read_text()
            self.assertTrue(text.startswith("---"), f"{command.name} has no frontmatter")
            self.assertIn("description:", text)
            for script in re.findall(r"scripts/(\w+\.py)", text):
                self.assertTrue(
                    (self.repo / "scripts" / script).is_file(),
                    f"{command.name} references scripts/{script}, which does not exist",
                )


    # -- create_component.py ------------------------------------------------------------------

    def test_create_component_lands_in_core_ui_by_default(self) -> None:
        self.run_script("create_component.py", "PrimaryButton")

        component = self.repo / f"core/ui/src/main/kotlin/{BASE_PATH}/core/ui/component/PrimaryButton.kt"
        self.assertTrue(component.is_file())
        text = component.read_text()
        self.assertIn(f"package {BASE_PATH.replace('/', '.')}.core.ui.component", text)
        # The convention doctor.py enforces, generated rather than remembered.
        self.assertIn("modifier: Modifier = Modifier", text)
        self.assertIn("@ComponentPreview", text)

    def test_create_component_in_a_feature(self) -> None:
        self.run_script("create_component.py", "ProductCard", "--feature", "catalog")

        component = self.presentation("catalog") / "ProductCard.kt"
        self.assertTrue(component.is_file())
        self.assertIn("feature.catalog.presentation", component.read_text())

    def test_create_component_with_state_generates_a_preview_fixture(self) -> None:
        self.run_script("create_component.py", "ProductCard", "--feature", "catalog", "--state")

        state = self.presentation("catalog") / "ProductCardState.kt"
        self.assertTrue(state.is_file())
        state_text = state.read_text()
        # doctor.py requires a PREVIEW on every *State.kt in a presentation module.
        self.assertIn("val PREVIEW", state_text)
        self.assertIn("@Immutable", state_text)
        self.assertIn("state: ProductCardState", (self.presentation("catalog") / "ProductCard.kt").read_text())
        self.assert_doctor_passes()

    def test_create_component_sub_package(self) -> None:
        self.run_script("create_component.py", "Badge", "--feature", "catalog", "--sub", "product")

        component = self.presentation("catalog") / "product/Badge.kt"
        self.assertTrue(component.is_file())
        self.assertIn("feature.catalog.presentation.product", component.read_text())

    def test_create_component_refuses_an_unknown_feature(self) -> None:
        result = self.run_script(
            "create_component.py", "Ghost", "--feature", "nosuchfeature", expect_success=False,
        )
        self.assertNotEqual(0, result.returncode)


    # -- init_project.py ----------------------------------------------------------------------

    def test_init_project_renames_the_package_everywhere(self) -> None:
        self.run_script("init_project.py", "--package", "com.acme.tracker", "--name", "Field Tracker")

        # No source file may still name the template — except the script itself, which keeps the
        # constants so it can rename a second project later.
        leftovers = []
        for path in sorted(self.repo.rglob("*")):
            if not path.is_file() or path.suffix not in {".kt", ".kts", ".xml", ".toml", ".py", ".md"}:
                continue
            relative = path.relative_to(self.repo)
            if {"build", ".gradle", ".git", "__pycache__"}.intersection(relative.parts):
                continue
            # init_project.py keeps the constants so it can rename a second project later, and
            # this file necessarily contains the very strings it is asserting the absence of.
            if path.name in {"init_project.py", "test_scripts.py"}:
                continue
            text = path.read_text()
            if TEMPLATE_PACKAGE_WORD in text or TEMPLATE_PROJECT_NAME in text:
                leftovers.append(str(relative))
        self.assertEqual([], leftovers, "these files still name the template")

    def test_init_project_moves_every_source_set(self) -> None:
        self.run_script("init_project.py", "--package", "com.acme.tracker", "--name", "Field Tracker")

        self.assertTrue((self.repo / "service/core/ui/src/main/kotlin/com/acme/tracker/core/ui").is_dir())
        # testFixtures and test source sets move too, not just main.
        self.assertTrue(
            (self.repo / "service/core/ui/src/testFixtures/kotlin/com/acme/tracker/core/ui/test").is_dir()
        )
        self.assertTrue((self.repo / "app/src/main/kotlin/com/acme/tracker/MainActivity.kt").is_file())
        self.assertFalse((self.repo / "service/core/ui/src/main/kotlin/com/example").exists())

    def test_init_project_updates_the_project_name_and_label(self) -> None:
        self.run_script("init_project.py", "--package", "com.acme.tracker", "--name", "Field Tracker")

        self.assertIn('rootProject.name = "FieldTracker"', self.read("settings.gradle.kts"))
        # The applicationId and every namespace derive from this one property — see build-logic/.
        self.assertIn("basePackage=com.acme.tracker", self.read("gradle.properties"))
        # The Gradle name is safe in a theme; the launcher label is the human-readable one.
        self.assertIn("Theme.FieldTracker", self.read("app/src/main/res/values/themes.xml"))
        self.assertIn(">Field Tracker<", self.read("app/src/main/res/values/strings.xml"))

    def test_init_project_leaves_the_generators_working(self) -> None:
        """
        The regression this guards: `_common.py` holds the package as dots and this file holds it
        as slashes. Rewriting only the dotted form leaves the script suite pointed at a directory
        the rename has just emptied.
        """
        self.run_script("init_project.py", "--package", "com.acme.tracker", "--name", "Field Tracker")

        self.assertIn('BASE_PACKAGE = "com.acme.tracker"', self.read("scripts/_common.py"))
        self.assertIn('BASE_PATH = "com/acme/tracker"', self.read("scripts/test_scripts.py"))

        # And the generators still work against the renamed project.
        self.run_script("create_feature.py", "billing")
        self.assertTrue(
            (self.repo / "feature/billing/presentation/src/main/kotlin/com/acme/tracker"
             "/feature/billing/presentation/BillingViewModel.kt").is_file()
        )
        self.assert_doctor_passes()

    def test_init_project_rejects_an_invalid_package(self) -> None:
        result = self.run_script(
            "init_project.py", "--package", "Com.Acme", "--name", "Field Tracker",
            expect_success=False,
        )
        self.assertNotEqual(0, result.returncode)
        self.assertIn("not a valid", result.stdout + result.stderr)

    def test_init_project_dry_run_changes_nothing(self) -> None:
        before = self.read("settings.gradle.kts")

        self.run_script(
            "init_project.py", "--package", "com.acme.tracker", "--name", "Field Tracker", "--dry-run",
        )

        self.assertEqual(before, self.read("settings.gradle.kts"))
        self.assertTrue((self.repo / f"service/core/ui/src/main/kotlin/{BASE_PATH}").is_dir())


    def test_export_service_rewrites_the_package_and_the_catalog(self) -> None:
        target = Path(self._temporary.name) / "target"
        self.run_script("export_service.py", "--to", str(target), "--package", "com.acme.myapp", "--sync-versions")

        self.assertTrue((target / "service/core/ui/src/main/kotlin/com/acme/myapp/core/ui/component/Screen.kt").is_file())

        # The service build files apply convention.* plugins, so build-logic/ has to come too.
        self.assertTrue((target / "build-logic/settings.gradle.kts").is_file())
        self.assertTrue((target / "build-logic/src/main/kotlin/AndroidLibraryConventionPlugin.kt").is_file())

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
        # All seven convention plugins, not just the two the service modules happen to apply.
        self.assertIn("convention-feature-presentation", catalog["plugins"])
        self.assertIn("convention-android-library", catalog["plugins"])
        # Declared only inside a convention plugin, as libs.findLibrary("androidx-compose-bom") —
        # no service build file names it any more.
        self.assertIn("androidx-compose-bom", catalog["libraries"])
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
