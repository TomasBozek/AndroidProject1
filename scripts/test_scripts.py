#!/usr/bin/env python3
"""
Smoke tests for the scaffolding scripts.

    python3 scripts/test_scripts.py
    python3 scripts/test_scripts.py -v ScaffoldingTest.test_delete_feature_restores_every_registration
    python3 scripts/test_scripts.py --with-gradle

Each test copies the repository's sources into a temporary directory and runs the scripts there as
subprocesses, so nothing touches the working tree. They check the registrations and the generated
text — not that the result compiles; T1 is still the real gate (`CLAUDE.md` § Checks).

`--with-gradle` adds the one test that does compile what a generator wrote. It costs minutes
rather than seconds, so it is off by default and CI runs it on the weekly schedule only.

Plain `unittest`, so there is no dependency to install.
"""

from __future__ import annotations

import os
import re
import shutil
import subprocess
import sys
import tempfile
import unittest
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parent.parent
BASE_PATH = "com/example/androidproject1"

# Read here rather than in `__main__`, because `skipUnless` below is evaluated when the class is
# defined — which happens on import, before `__main__` would have had a chance to look at argv.
WITH_GRADLE = "--with-gradle" in sys.argv
if WITH_GRADLE:
    sys.argv.remove("--with-gradle")

# Built rather than written out: init_project.py rewrites the literal forms, so spelling them here
# would leave this file asserting against whatever the project was renamed to.
TEMPLATE_PACKAGE_WORD = "android" + "project1"
TEMPLATE_PROJECT_NAME = "Android" + "Project1"

# Build output, IDE and agent state; everything else is copied so the scripts see a realistic repo.
# `screenshots` is the Roborazzi goldens (ui.2) — megabytes of PNG that no script reads, copied
# once per test. What matters about them, that a clone does not carry them, is asserted by
# `test_create_feature_does_not_clone_the_goldens`, which writes one of its own.
_IGNORED_NAMES = shutil.ignore_patterns(
    "build", ".gradle", ".git", ".idea", ".kotlin", "__pycache__", ".DS_Store", "screenshots"
)


def ignore_for_copy(directory: str, names: list[str]) -> set[str]:
    """
    The patterns above, plus `.claude/worktrees` — the one that got away.

    Each agent worktree is a checkout of this same repository, so copying them meant the copy was
    mostly copies of itself: four of them were 2,271 of the 2,856 files this copied, 56 times over,
    and the suite went from the twenty seconds documented to between four and seven minutes with
    nothing failing to say so. `test_the_repository_copy_stays_small` is what should have caught it.

    Named rather than pattern-matched, because the rest of `.claude` has to be copied:
    `.claude/commands` holds the slash commands that
    `test_slash_commands_reference_scripts_that_exist` reads out of the copy.
    """
    ignored = set(_IGNORED_NAMES(directory, names))
    if Path(directory).name == ".claude":
        ignored.add("worktrees")
    return ignored


# What one test's copy of the repository may hold, with room for the project to grow: today it is
# about 600 files. A directory that pushes it past this belongs in `ignore_for_copy` above.
MAX_COPIED_FILES = 1500


def hermetic_env() -> dict[str, str]:
    """
    The ambient environment minus git's own variables.

    A git hook runs with GIT_DIR, GIT_INDEX_FILE and friends set, and in a worktree GIT_DIR is an
    absolute path. Every `git` these tests run inside their temp copy would otherwise operate on
    the real repository: `git init` initialising the wrong gitdir, `git status` reporting the
    commit in progress as uncommitted changes and making init_project.py refuse. That is what made
    the pre-commit hook fail on exactly the commits it exists to check.
    """
    return {key: value for key, value in os.environ.items() if not key.startswith("GIT_")}


class ScaffoldingTest(unittest.TestCase):

    def setUp(self) -> None:
        self._temporary = tempfile.TemporaryDirectory()
        self.addCleanup(self._temporary.cleanup)
        self.repo = Path(self._temporary.name) / "repo"
        shutil.copytree(REPO_ROOT, self.repo, ignore=ignore_for_copy)

    # -- helpers ------------------------------------------------------------------------------

    def run_script(self, name: str, *args: str, expect_success: bool = True) -> subprocess.CompletedProcess:
        result = subprocess.run(
            [sys.executable, str(self.repo / "scripts" / name), *args],
            capture_output=True,
            text=True,
            env=hermetic_env(),
        )
        if expect_success and result.returncode != 0:
            self.fail(f"{name} {' '.join(args)} failed:\n{result.stdout}\n{result.stderr}")
        return result

    def read(self, relative: str) -> str:
        return (self.repo / relative).read_text()

    def presentation(self, feature: str, source_set: str = "main") -> Path:
        return self.repo / f"feature/{feature}/presentation/src/{source_set}/kotlin/{BASE_PATH}/feature/{feature}/presentation"

    def screen(self, feature: str, name: str, source_set: str = "main") -> Path:
        """A screen's directory since D34: `presentation/<the screen name, flat lowercase>/`."""
        return self.presentation(feature, source_set) / name.lower()

    def component(self, feature: str) -> Path:
        """A feature's own components, since D34: `presentation/component/`."""
        return self.presentation(feature) / "component"

    def assert_doctor_passes(self) -> None:
        result = self.run_script("doctor.py", expect_success=False)
        self.assertEqual(0, result.returncode, f"doctor.py reported problems:\n{result.stdout}")

    def resource_names(self, strings_xml: str) -> set[str]:
        return set(re.findall(r'<(?:string|plurals) name="([^"]+)"', strings_xml))

    def tree_snapshot(self) -> set[tuple[str, int]]:
        return {
            (str(p.relative_to(self.repo)), p.stat().st_size)
            for p in self.repo.rglob("*")
            if p.is_file() and "__pycache__" not in p.parts
        }

    # -- tests --------------------------------------------------------------------------------

    def test_doctor_passes_on_a_clean_checkout(self) -> None:
        self.assert_doctor_passes()

    def test_the_repository_copy_stays_small(self) -> None:
        """
        Every test copies the repository, so anything that joins the copy is paid 56 times over.
        This asserts the thing that is easy to regress and impossible to notice: the suite has no
        other way of telling you it has become fourteen times slower, because it still passes.
        """
        copied = sum(len(names) for _, _, names in os.walk(self.repo))
        self.assertLess(
            copied,
            MAX_COPIED_FILES,
            f"a test's copy of the repository holds {copied} files, over the {MAX_COPIED_FILES} "
            "budget. Add whatever directory grew to `ignore_for_copy`, or raise the budget in the "
            "same commit that grew the repository, and say which directory it was.",
        )

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
        self.assertIn(
            "import com.example.androidproject1.feature.userprofile.presentation.userprofile.userProfileDestination",
            nav_host,
        )
        self.assertNotIn("userprofileDestination(", nav_host)

        destination = (self.screen("userprofile", "UserProfile") / "UserProfileDestination.kt").read_text()
        self.assertIn("fun EntryProviderScope<NavKey>.userProfileDestination(", destination)

        # The fifth registration: the module tree in docs/ai/CODEBASE.md, which doctor.py checks.
        self.assertIn(
            ":feature:userprofile:{domain,data,presentation,di}",
            self.read("docs/ai/CODEBASE.md"),
        )

        self.assert_doctor_passes()

    def test_create_component_lists_a_core_ui_component_in_the_gallery(self) -> None:
        """The one registration a component has (D52): doctor.py fails on a component with none."""
        self.run_script("create_component.py", "PriceTicker")

        catalog = self.read(
            "feature/gallery/presentation/src/main/kotlin/"
            f"{BASE_PATH}/feature/gallery/presentation/GalleryCatalog.kt"
        )
        self.assertIn('"priceTicker", "PriceTicker"', catalog)
        self.assert_doctor_passes()

    def test_create_component_for_a_feature_stays_out_of_the_gallery(self) -> None:
        """The gallery lists `:core:ui`, which is the set every feature may compose from."""
        self.run_script("create_feature.py", "userProfile")
        self.run_script("create_component.py", "ProductCard", "--feature", "userprofile")

        catalog = self.read(
            "feature/gallery/presentation/src/main/kotlin/"
            f"{BASE_PATH}/feature/gallery/presentation/GalleryCatalog.kt"
        )
        self.assertNotIn("ProductCard", catalog)
        self.assert_doctor_passes()

    def test_create_feature_does_not_clone_the_goldens(self) -> None:
        """The screenshot test is cloned; the images it recorded for the template are not.

        A generated feature's previews are named after the feature, so the template's goldens
        match nothing in it — they would sit there unverified while `verifyRoborazziDebug` failed
        on the images that are genuinely missing. `recordRoborazziDebug` writes the right ones.
        """
        goldens = self.repo / "feature/template/presentation/src/test/screenshots"
        goldens.mkdir(parents=True, exist_ok=True)
        (goldens / "TemplateScreenKt.Preview.Light.png").write_bytes(b"\x89PNG\r\n")

        self.run_script("create_feature.py", "userProfile")

        self.assertFalse(
            (self.repo / "feature/userprofile/presentation/src/test/screenshots").exists(),
            "the template's goldens were cloned into the generated feature",
        )

        test = self.presentation("userprofile", "test") / "screenshot/PreviewScreenshotTest.kt"
        self.assertTrue(test.is_file(), "the screenshot test was not cloned")
        # The package tree it scans, which is the one thing that differs between the copies — the
        # rest is `PreviewScreenshotSpec`. Asserted as the string rather than as the call around it,
        # so reformatting the generated file does not fail this.
        contents = test.read_text()
        self.assertIn('"com.example.androidproject1.feature.userprofile.presentation"', contents)
        self.assertIn("PreviewScreenshotSpec", contents)
        self.assertNotIn("template", contents)

    def test_create_feature_renames_string_resources(self) -> None:
        """The template's `template_title` must not survive into a generated feature."""
        self.run_script("create_feature.py", "userProfile")

        strings = self.read("feature/userprofile/presentation/src/main/res/values/strings.xml")
        self.assertIn('name="user_profile_title"', strings)
        self.assertNotIn("template_", strings)

        screen = (self.screen("userprofile", "UserProfile") / "UserProfileScreen.kt").read_text()
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
        self.assertIn(":feature:userprofile:{presentation,di}", self.read("docs/ai/CODEBASE.md"))

        self.run_script("create_feature.py", "userProfile", "--layers", "domain", "--force")
        self.assertIn(":feature:userprofile:{domain,presentation,di}", self.read("docs/ai/CODEBASE.md"))
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

        # D34: the unit lands in a directory named after the screen, not in the flat package.
        directory = self.screen("userprofile", "UserProfileList")
        for suffix in ("Destination", "Screen", "State", "Event", "Navigation", "ViewModel"):
            self.assertTrue((directory / f"UserProfileList{suffix}.kt").is_file(), suffix)
        self.assertFalse((self.presentation("userprofile") / "UserProfileListScreen.kt").exists())

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
        self.assertFalse((self.presentation("userprofile") / "userprofiledetail").exists())
        self.assertIn("package com.example.androidproject1.feature.userprofile.presentation.detail", screen)
        self.assertIn("import com.example.androidproject1.feature.userprofile.presentation.R", screen)

        module = self.read(f"feature/userprofile/di/src/main/kotlin/{BASE_PATH}/feature/userprofile/di/UserProfileModule.kt")
        self.assertIn(
            "import com.example.androidproject1.feature.userprofile.presentation.detail.UserProfileDetailViewModel",
            module,
        )
        self.assert_doctor_passes()

    def test_create_feature_translates_the_strings_it_renames(self) -> None:
        """feat.9: a generated feature starts bilingual, or doctor.py fails on the next commit."""
        self.run_script("create_feature.py", "userProfile")

        czech = self.read("feature/userprofile/presentation/src/main/res/values-cs/strings.xml")
        self.assertIn('name="user_profile_title"', czech)
        self.assertNotIn("template_", czech)

        default = self.read("feature/userprofile/presentation/src/main/res/values/strings.xml")
        self.assertEqual(
            self.resource_names(default),
            self.resource_names(czech),
            "values/ and values-cs/ must declare the same names",
        )

    def test_create_screen_adds_its_strings_to_every_locale(self) -> None:
        """The bug this exists for: the new screen's Czech was merged into `values/` only."""
        self.run_script("create_feature.py", "userProfile")
        self.run_script("create_screen.py", "userprofile", "UserProfileList")

        for directory in ("values", "values-cs"):
            strings = self.read(f"feature/userprofile/presentation/src/main/res/{directory}/strings.xml")
            self.assertIn('name="user_profile_list_title"', strings, directory)
            self.assertNotIn("template_", strings, directory)

        self.assert_doctor_passes()

    def test_create_screen_with_args_adds_its_strings_to_every_locale(self) -> None:
        """The args screen has its own `template_args_*` set, which is renamed separately."""
        self.run_script("create_feature.py", "userProfile")
        self.run_script("create_screen.py", "userprofile", "UserProfileDetail", "--with-args", "userId:String")

        for directory in ("values", "values-cs"):
            strings = self.read(f"feature/userprofile/presentation/src/main/res/{directory}/strings.xml")
            self.assertIn('name="user_profile_detail_title"', strings, directory)

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
        self.assertIn("import com.example.androidproject1.service.core.domain.coroutines.DispatcherProvider", implementation)
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
            "docs/ai/CODEBASE.md",
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

        destination = (self.screen("catalog", "ProductReview") / "ProductReviewDestination.kt").read_text()
        self.assertIn(
            "data class ProductReviewDestination(val productId: String, val rating: Int) : NavKey",
            destination,
        )
        # The key goes straight into the ViewModel, not into a load() call from here. Match the
        # call, not the word: the template's comment explains why LaunchedEffect is wrong.
        self.assertIn("parametersOf(key)", destination)
        self.assertNotIn("LaunchedEffect(", destination)
        self.assertNotIn("viewModel.load(", destination)

        view_model = (self.screen("catalog", "ProductReview") / "ProductReviewViewModel.kt").read_text()
        self.assertIn("private val args: ProductReviewDestination", view_model)
        self.assertNotIn("SavedStateHandle", view_model)

        # The sixth registration: Koin's verify() cannot see a parametersOf argument.
        graph_test = self.read(f"app/src/test/kotlin/{BASE_PATH}/KoinGraphTest.kt")
        self.assertIn("definition<ProductReviewViewModel>(ProductReviewDestination::class)", graph_test)

    def test_create_screen_with_args_generates_a_plain_jvm_test(self) -> None:
        self.run_script("create_screen.py", "catalog", "ProductReview", "--with-args", "productId:String")

        test = (
            self.screen("catalog", "ProductReview", "test") / "ProductReviewViewModelTest.kt"
        ).read_text()
        # Navigation 3 hands the key over as a plain object, so there is no Bundle to decode and
        # no Robolectric runner.
        self.assertIn("ProductReviewDestination(productId = \"example\")", test)
        self.assertNotIn("RobolectricTestRunner", test)

    def test_create_datasource_remote_generates_a_ktor_source(self) -> None:
        self.run_script("create_datasource.py", "catalog", "RemoteWeather", "--remote", "--repository")

        source = self.read(
            f"feature/catalog/data/src/main/kotlin/{BASE_PATH}"
            "/feature/catalog/data/source/DefaultRemoteWeatherDataSource.kt"
        )
        # Ktor, not DataStore — and the status table is applied at the source, so the repository
        # above it sees a DomainError and never an HTTP code.
        self.assertIn("io.ktor.client.HttpClient", source)
        self.assertIn("HttpErrorMapper.map(throwable)", source)
        self.assertNotIn("DataStoreProvider", source)
        # The DTO is the wire shape and stays in the data layer.
        self.assertIn("data class RemoteWeatherDto", source)

        repository = self.read(
            f"feature/catalog/data/src/main/kotlin/{BASE_PATH}"
            "/feature/catalog/data/repository/DefaultWeatherRepository.kt"
        )
        self.assertIn("execute {", repository)
        # The repository returns the domain type; the DTO does not escape the data layer.
        self.assertIn("Outcome<Weather>", repository)
        self.assertNotIn("RemoteWeatherDto", repository)

    def test_create_datasource_without_remote_is_unchanged(self) -> None:
        """The default path must not pick up the Ktor template."""
        self.run_script("create_datasource.py", "catalog", "LocalWeather", "--repository")

        source = self.read(
            f"feature/catalog/data/src/main/kotlin/{BASE_PATH}"
            "/feature/catalog/data/source/DefaultLocalWeatherDataSource.kt"
        )
        self.assertIn("DataStoreProvider", source)
        self.assertNotIn("io.ktor", source)

    def test_create_screen_generates_a_screen_test(self) -> None:
        self.run_script("create_screen.py", "catalog", "ProductReview")

        test = (
            self.screen("catalog", "ProductReview", "test") / "ProductReviewScreenTest.kt"
        ).read_text()
        # The half a ViewModel test cannot reach: what is on screen, and what a tap does.
        self.assertIn("class ProductReviewScreenTest", test)
        self.assertIn("RobolectricTestRunner", test)
        self.assertNotIn("Template", test)

    def test_a_generated_screen_and_its_test_agree_on_the_tag(self) -> None:
        """A tag's stem is camelCase; the resource beside it is snake_case. They rewrite apart."""
        self.run_script("create_screen.py", "catalog", "ProductReview")

        screen = (self.screen("catalog", "ProductReview") / "ProductReviewScreen.kt").read_text()
        test = (self.screen("catalog", "ProductReview", "test") / "ProductReviewScreenTest.kt").read_text()

        # camelCase stem in both, so the test can actually find what the screen tags.
        self.assertIn('testTag("productReview_incrementButton")', screen)
        self.assertIn('onNodeWithTag("productReview_incrementButton")', test)
        # ...while the string resource it sits next to stays snake_case.
        self.assertIn("R.string.product_review_increment", screen)
        self.assertNotIn("product_review_incrementButton", screen + test)

    def test_create_screen_with_args_defaults_to_a_single_id(self) -> None:
        self.run_script("create_screen.py", "catalog", "ProductReview", "--with-args")

        self.assertIn(
            "data class ProductReviewDestination(val id: String)",
            (self.screen("catalog", "ProductReview") / "ProductReviewDestination.kt").read_text(),
        )

    def test_create_screen_with_args_names_files_without_the_args_suffix(self) -> None:
        """The template class is `TemplateArgs`; the generated files must not inherit the suffix."""
        self.run_script("create_screen.py", "catalog", "ProductReview", "--with-args")

        self.assertTrue((self.screen("catalog", "ProductReview") / "ProductReviewViewModel.kt").is_file())
        self.assertFalse((self.screen("catalog", "ProductReview") / "ProductReviewArgsViewModel.kt").exists())
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

        destination = (self.screen("catalog", "PlainScreen") / "PlainScreenDestination.kt").read_text()
        self.assertIn("data object PlainScreenDestination", destination)
        self.assertFalse((self.screen("catalog", "PlainScreen") / "PlainScreenArgsDestination.kt").exists())
        view_model = (self.screen("catalog", "PlainScreen") / "PlainScreenViewModel.kt").read_text()
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
                env=hermetic_env(),
            )
            self.assertEqual(0, result.returncode, f"{script.name} --help failed:\n{result.stderr}")
            self.assertIn(
                f"scripts/{script.name}", result.stdout,
                f"{script.name} --help has no worked example",
            )
            self.assertIn(script.name, readme, f"scripts/README.md does not mention {script.name}")


    # -- board.py -----------------------------------------------------------------------------

    def test_board_reads_the_open_sprint_and_refuses_a_second(self) -> None:
        """One JSON document from docs/: the open sprint with its briefs joined onto the board lines,
        the grouped backlog, what shipped — and a loud failure rather than half a board."""
        import json
        result = self.run_script("board.py")
        board = json.loads(result.stdout)
        self.assertIsNotNone(board["sprint"], "no sprint is Status: open")
        sprint = board["sprint"]
        self.assertRegex(sprint["id"], r"^[A-Z][0-9]$")
        self.assertTrue(sprint["start"] and sprint["goal"] and sprint["tasks"])
        first = sprint["tasks"][0]
        for key in ("id", "state", "title", "est", "why", "doneWhen"):
            self.assertTrue(first.get(key) not in (None, ""), f"task lacks {key}")
        self.assertEqual(sprint["points"], sum(t["est"] for t in sprint["tasks"]))
        self.assertIsNotNone(board["release"], "no release is Status: open")
        for section, items in board["backlog"].items():
            for item in items:
                self.assertTrue(item["group"], f"§ {section} line without a group: {item['title']}")
                for key in ("kind", "area", "feature", "layer"):
                    self.assertIn(key, item, f"§ {section} line without `{key}`: {item['title']}")
                self.assertEqual(item["area"], item["group"].split(":")[0])
        self.assertTrue(board["shipped"] and board["shipped"][0]["version"].startswith("v"))

        # A second open sprint is a half board, and the script says so instead of printing one.
        plan = self.repo / "docs/ai/plans" / Path(sprint["file"]).name
        second = plan.with_name("Z9-second.md")
        second.write_text(plan.read_text().replace(f"# Sprint {sprint['id']}", "# Sprint Z9").replace(f"Sprint: {sprint['id']}", "Sprint: Z9"))
        result = self.run_script("board.py", expect_success=False)
        self.assertNotEqual(0, result.returncode)
        self.assertIn("more than one sprint", result.stderr)

    def test_backlog_grammar_is_held_by_both_scripts(self) -> None:
        """D69: `- <title> · <pts> · <group> [· <kind>] · <why>` in every section. board.py refuses a
        line with no group; doctor.py names each broken part, and a `?` where a band is due."""
        import json
        backlog = self.repo / "docs/BACKLOG.md"
        good = self.read("docs/BACKLOG.md")
        self.assertGreaterEqual(good.count("\n- "), 40, "§ Someday is one idea per line")
        derived = {(i["group"], i["kind"], i["area"], i["feature"], i["layer"])
                   for items in json.loads(self.run_script("board.py").stdout)["backlog"].values() for i in items}
        self.assertIn(("feature:auth:data", "H", "feature", "auth", "data"), derived)
        self.assertIn(("core:ui", "X", "core", None, None), derived)
        self.assertIn(("release", "P", "release", None, None), derived)

        def with_next(line: str) -> None:
            backlog.write_text(good.replace("## Next\n", f"## Next\n\n{line}\n", 1))

        def doctor_says(line: str, *fragments: str) -> None:
            with_next(line)
            result = self.run_script("doctor.py", expect_success=False)
            self.assertNotEqual(0, result.returncode, line)
            self.assertIn("[FAIL] every backlog line is", result.stdout, line)
            for fragment in fragments:
                self.assertIn(fragment, result.stdout, line)

        with_next("- A line with no group · 3 · nothing says what it touches")
        result = self.run_script("board.py", expect_success=False)
        self.assertNotEqual(0, result.returncode)
        self.assertIn("has no group", result.stderr)
        doctor_says("- A line with no group · 3 · nothing says what it touches", "a part is missing")
        doctor_says("- A line with a bad kind · 3 · core:ui · Q · a letter outside the six", "`Q` is not a kind")
        doctor_says("- A line with a bad band · 4 · core:ui · not a band", "`4` is not a band")
        doctor_says("- A line under no root · 3 · kotlin:ui · a root the tree has not", "neither a module root nor a process area")
        doctor_says("- An unsized line under Next · ? · core:ui · fine elsewhere", "`?` is allowed only under § Someday")
        backlog.write_text(good)
        self.assert_doctor_passes()

    # -- .githooks -----------------------------------------------------------------------------

    def test_pre_commit_hook_is_executable_and_runs_doctor(self) -> None:
        hook = REPO_ROOT / ".githooks/pre-commit"
        self.assertTrue(hook.is_file())
        self.assertTrue(hook.stat().st_mode & 0o111, "hook is not executable")
        text = hook.read_text()
        self.assertIn("doctor.py", text)
        # test_scripts.py takes ~20s, so it runs only when the commit touches the generators.
        self.assertIn("grep -q '^scripts/'", text)

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

        # D34: a feature's own components live in `component/`, never beside a screen.
        component = self.component("catalog") / "ProductCard.kt"
        self.assertTrue(component.is_file())
        self.assertIn("feature.catalog.presentation.component", component.read_text())

    def test_create_component_with_state_generates_a_preview_fixture(self) -> None:
        self.run_script("create_component.py", "ProductCard", "--feature", "catalog", "--state")

        state = self.component("catalog") / "ProductCardState.kt"
        self.assertTrue(state.is_file())
        state_text = state.read_text()
        # doctor.py requires a PREVIEW on every *State.kt in a presentation module.
        self.assertIn("val PREVIEW", state_text)
        self.assertIn("@Immutable", state_text)
        self.assertIn("state: ProductCardState", (self.component("catalog") / "ProductCard.kt").read_text())
        self.assert_doctor_passes()

    def test_create_component_sub_package(self) -> None:
        self.run_script("create_component.py", "Badge", "--feature", "catalog", "--sub", "product")

        component = self.component("catalog") / "product/Badge.kt"
        self.assertTrue(component.is_file())
        self.assertIn("feature.catalog.presentation.component.product", component.read_text())

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
            if not path.is_file() or path.suffix not in {".kt", ".kts", ".xml", ".toml", ".py", ".md", ".conf"}:
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

        self.assertTrue((self.repo / "service/core/ui/src/main/kotlin/com/acme/tracker/service/core/ui").is_dir())
        # testFixtures and test source sets move too, not just main.
        self.assertTrue(
            (self.repo / "service/core/ui/src/testFixtures/kotlin/com/acme/tracker/service/core/ui/test").is_dir()
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

    def test_init_project_rewrites_the_licence_holder(self) -> None:
        """
        `LICENSE` has no suffix, so the suffix-driven walk never saw it and every project generated
        from this template shipped the template's own copyright holder.
        """
        self.run_script(
            "init_project.py", "--package", "com.acme.tracker", "--name", "Field Tracker",
            "--author", "Acme Ltd",
        )

        licence = self.read("LICENSE")
        self.assertIn("Copyright (c) 2026 Acme Ltd.", licence)
        self.assertNotIn("Božek", licence)
        # The terms are the new owner's to change; only the holder moves.
        self.assertIn("No licence is granted.", licence)

    def test_init_project_defaults_the_licence_holder_to_the_app_name(self) -> None:
        """--author is optional, but shipping someone else's name is never the default."""
        self.run_script("init_project.py", "--package", "com.acme.tracker", "--name", "Field Tracker")

        self.assertIn("Copyright (c) 2026 Field Tracker.", self.read("LICENSE"))

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
             "/feature/billing/presentation/billing/BillingViewModel.kt").is_file()
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


    def test_export_service_rewrites_the_package(self) -> None:
        target = Path(self._temporary.name) / "target"
        self.run_script("export_service.py", "--to", str(target), "--package", "com.acme.myapp")

        self.assertTrue((target / "service/core/ui/src/main/kotlin/com/acme/myapp/service/core/ui/component/Screen.kt").is_file())

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

        # No catalog merge any more: the printed output points at this project's own
        # gradle/libs.versions.toml instead of writing one.
        result = self.run_script("export_service.py", "--to", str(target), "--package", "com.acme.myapp", "--force")
        self.assertIn("gradle/libs.versions.toml", result.stdout)
        self.assertFalse((target / "gradle/libs.versions.toml").exists())

    def test_doctor_catches_an_unregistered_view_model(self) -> None:
        self.run_script("create_feature.py", "userProfile")
        module = self.repo / f"feature/userprofile/di/src/main/kotlin/{BASE_PATH}/feature/userprofile/di/UserProfileModule.kt"
        module.write_text(module.read_text().replace("viewModelOf(::UserProfileViewModel)", ""))

        result = self.run_script("doctor.py", expect_success=False)
        self.assertNotEqual(0, result.returncode)
        self.assertIn("UserProfileViewModel", result.stdout)


    def test_doctor_catches_a_feature_drawing_its_own_ui(self) -> None:
        """The rule the design system rests on: a feature composes components, it never draws."""
        screen = self.screen("home", "Home") / "HomeScreen.kt"

        original = screen.read_text()
        for violation, expected in (
            ("import androidx.compose.material3.Button\n", "Material's Button"),
            ("import androidx.compose.foundation.layout.Row\n@Suppress\nval pad = 12.dp\n", "12.dp"),
        ):
            screen.write_text(violation + original)
            result = self.run_script("doctor.py", expect_success=False)
            self.assertNotEqual(0, result.returncode)
            self.assertIn(expected, result.stdout)
        screen.write_text(original)

    def test_init_project_renames_the_launcher_label(self) -> None:
        """The flavors compose their labels from `appName`, so the rename has to reach it."""
        self.run_script("init_project.py", "--package", "com.acme.tracker", "--name", "Field Tracker")

        properties = self.read("gradle.properties")
        self.assertIn("appName=Field Tracker", properties)
        self.assertIn("basePackage=com.acme.tracker", properties)
        self.assertNotIn("AndroidProject1", properties)

    def test_doctor_catches_a_state_that_is_not_immutable(self) -> None:
        state = self.screen("home", "Home") / "HomeState.kt"
        state.write_text(state.read_text().replace("@Immutable\n", ""))

        result = self.run_script("doctor.py", expect_success=False)
        self.assertNotEqual(0, result.returncode)
        self.assertIn("HomeState", result.stdout)
        self.assertIn("@Immutable", result.stdout)

    def test_generated_screens_are_immutable(self) -> None:
        """A generated state must pass the check the moment it is written."""
        self.run_script("create_feature.py", "userProfile")
        self.run_script("create_screen.py", "userprofile", "UserProfileDetail")
        self.assert_doctor_passes()

    def test_doctor_catches_a_component_without_a_preview(self) -> None:
        component = self.repo / f"core/ui/src/main/kotlin/{BASE_PATH}/core/ui/component/AppButton.kt"
        component.write_text(component.read_text().replace("@ComponentPreview", "@Suppress(\"unused\")"))

        result = self.run_script("doctor.py", expect_success=False)
        self.assertNotEqual(0, result.returncode)
        self.assertIn("AppButton.kt", result.stdout)
        self.assertIn("@ComponentPreview", result.stdout)

    def test_doctor_catches_a_koin_built_class_with_a_defaulted_parameter(self) -> None:
        """The Trips crash: Koin's `*Of` builders resolve every parameter through `get()` and never
        consult a Kotlin default, so a defaulted `Clock` nobody bound compiled, read as safe, passed
        `verify()` and threw the first time the screen opened.
        """
        view_model = self.repo / (
            "feature/trips/presentation/src/main/kotlin/"
            f"{BASE_PATH}/feature/trips/presentation/trips/TripsViewModel.kt"
        )
        view_model.write_text(
            view_model.read_text().replace(
                "private val clock: Clock,",
                "private val clock: Clock = Clock.systemDefaultZone(),",
                1,
            )
        )

        result = self.run_script("doctor.py", expect_success=False)
        self.assertNotEqual(0, result.returncode)
        self.assertIn("TripsViewModel", result.stdout)
        self.assertIn("bind the type instead", result.stdout)

    def test_doctor_catches_a_destination_missing_from_the_features_reference(self) -> None:
        """The reference is what an agent reads instead of the code, so a missing row is a wrong
        answer rather than a gap. It reads the table's first column only — a name mentioned in the
        prose below it used to satisfy this, which is how `Trips` passed while its row was gone.
        """
        reference = self.repo / "docs/ai/reference/FEATURES.md"
        reference.write_text(reference.read_text().replace("| `Trips` | trips |", "| `Zzz` | trips |", 1))

        result = self.run_script("doctor.py", expect_success=False)
        self.assertNotEqual(0, result.returncode)
        self.assertIn("TripsDestination.kt", result.stdout)
        self.assertIn("FEATURES.md", result.stdout)

    def test_doctor_catches_a_component_missing_from_the_design_system_reference(self) -> None:
        """The counterpart to the gallery check: the gallery is what a person browses, the table is
        what an agent greps before writing something that already exists.
        """
        reference = self.repo / "docs/ai/reference/DESIGN-SYSTEM.md"
        reference.write_text(reference.read_text().replace(" `AppAvatarPhoto`", "", 1))

        result = self.run_script("doctor.py", expect_success=False)
        self.assertNotEqual(0, result.returncode)
        self.assertIn("AppAvatarPhoto.kt", result.stdout)
        self.assertIn("DESIGN-SYSTEM.md", result.stdout)

    # -- the presentation layout (D34) --------------------------------------------------------

    def test_create_screen_brings_the_feature_component_it_composes(self) -> None:
        """The generated screen composes one feature-local component; it has to arrive with it."""
        self.run_script("create_screen.py", "catalog", "ProductReview")

        component = self.component("catalog") / "ProductReviewHeadline.kt"
        self.assertTrue(component.is_file())
        self.assertIn("feature.catalog.presentation.component", component.read_text())
        self.assertIn("@ComponentPreview", component.read_text())
        self.assertIn(
            "import com.example.androidproject1.feature.catalog.presentation.component.ProductReviewHeadline",
            (self.screen("catalog", "ProductReview") / "ProductReviewScreen.kt").read_text(),
        )
        self.assert_doctor_passes()

    def test_doctor_catches_a_composable_left_beside_a_screen(self) -> None:
        """The other half of D34: a screen file holds the screen and its previews, nothing else."""
        screen = self.screen("home", "Home") / "HomeScreen.kt"
        original = screen.read_text()
        screen.write_text(
            original + "\n@Composable\nprivate fun Stray(modifier: Modifier = Modifier) {\n}\n"
        )

        result = self.run_script("doctor.py", expect_success=False)
        self.assertNotEqual(0, result.returncode)
        self.assertIn("composable 'Stray' is not the screen", result.stdout)
        screen.write_text(original)

    def test_doctor_catches_a_stranger_in_a_screen_directory(self) -> None:
        stray = self.screen("home", "Home") / "Helpers.kt"
        stray.write_text("package com.example.androidproject1.feature.home.presentation.home\n")

        result = self.run_script("doctor.py", expect_success=False)
        self.assertNotEqual(0, result.returncode)
        self.assertIn("is not part of screen 'Home'", result.stdout)
    def test_doctor_catches_a_maestro_id_that_is_not_in_the_code(self) -> None:
        """A renamed tag breaks a flow silently; the grep is what finds it before the emulator."""
        flow = self.repo / ".maestro/sign-in.yaml"
        flow.write_text(flow.read_text().replace("login_emailField", "login_emailBox"))

        result = self.run_script("doctor.py", expect_success=False)
        self.assertNotEqual(0, result.returncode)
        self.assertIn("sign-in.yaml", result.stdout)
        self.assertIn("login_emailBox", result.stdout)

    def test_doctor_catches_a_library_in_a_module_build_file(self) -> None:
        """D64: a module build file is plugins, project dependencies and resourcePrefix. A library
        line belongs to the convention plugin the module applies."""
        build_file = self.repo / "feature/cart/domain/build.gradle.kts"
        build_file.write_text(build_file.read_text() + "\ndependencies {\n    implementation(libs.junit)\n}\n")

        result = self.run_script("doctor.py", expect_success=False)
        self.assertNotEqual(0, result.returncode)
        self.assertIn("feature/cart/domain/build.gradle.kts", result.stdout)
        self.assertIn("names a library", result.stdout)

    def test_doctor_catches_a_hand_written_test_fixtures_block(self) -> None:
        """The other half of D64: `convention.android.library.testfixtures` exists for this."""
        build_file = self.repo / "feature/cart/data/build.gradle.kts"
        build_file.write_text(build_file.read_text() + "\nandroid {\n    testFixtures {\n        enable = true\n    }\n}\n")

        result = self.run_script("doctor.py", expect_success=False)
        self.assertNotEqual(0, result.returncode)
        self.assertIn("feature/cart/data/build.gradle.kts", result.stdout)
        self.assertIn("enables test fixtures by hand", result.stdout)

    def test_doctor_catches_a_test_id_named_after_a_component(self) -> None:
        """D60: the vocabulary stays closed. A stepper in a form is a `Field`, never a `Stepper`."""
        screen = self.screen("home", "Home") / "HomeScreen.kt"
        original = screen.read_text()
        screen.write_text(original.replace(
            'testTag("home_favouritesList")', 'testTag("foo_barStepper")', 1,
        ))

        result = self.run_script("doctor.py", expect_success=False)
        self.assertNotEqual(0, result.returncode)
        self.assertIn("test id 'foo_barStepper' ends in no vocabulary word", result.stdout)

    @unittest.skipUnless(
        WITH_GRADLE,
        "compiles a generated feature; pass --with-gradle (minutes, not seconds)",
    )
    def test_generated_feature_compiles(self) -> None:
        """
        The half every other test here cannot reach.

        The rest of this file checks the *text* a generator wrote — the registrations, the names,
        the strings. Text can be perfectly correct and still not compile: an import the template
        stopped needing, a signature that moved in `:core:ui`, an `R` reference that a sub-package
        broke. `feature/template` is compiled by the ordinary build, so it is the *rewriting* that
        is unproven, and nothing notices until someone runs a generator and gets a red project.

        Compiles the presentation module, which is where the generated Compose, the `R` references
        and the navigation wiring all live, and so where a template change breaks first.
        """
        self.run_script("create_feature.py", "userProfile")

        result = subprocess.run(
            [
                "./gradlew",
                ":feature:userprofile:presentation:assembleDebug",
                "--console=plain",
                # The temp copy is thrown away, so a stored entry would be written for a project
                # directory that is about to stop existing.
                "--no-configuration-cache",
            ],
            cwd=self.repo,
            capture_output=True,
            text=True,
            env=hermetic_env(),
        )
        if result.returncode != 0:
            # The compiler's own message, not "exit 1" — this test failing on the weekly job is
            # someone's Monday, and the error should be in the log rather than reproducible only
            # by re-running it locally.
            self.fail(
                "a generated feature does not compile:\n"
                f"{result.stdout[-6000:]}\n{result.stderr[-4000:]}"
            )



if __name__ == "__main__":
    unittest.main(verbosity=2)
