#!/usr/bin/env python3
"""
Checks the conventions this template relies on but cannot enforce with a compiler.

    python3 scripts/doctor.py
    python3 scripts/doctor.py --list

There is no detekt/ktlint here (detekt 1.23 cannot read the JDK 25 the daemon is pinned to), so
these greps are the only automated defence for the rules in CLAUDE.md: the portability of
`service/`, the six-file screen unit, module and DI registration, and versions coming from the
version catalog. Exits non-zero when something is wrong, so it can run in CI.
"""

from __future__ import annotations

import argparse
import re
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))

from _common import (  # noqa: E402
    ALL_LAYERS,
    APP_NAV_HOST_FILE,
    KOIN_GRAPH_TEST_FILE,
    BASE_PACKAGE,
    CLAUDE_MD_FILE,
    CORE_DI_BUILD_FILE,
    FEATURE_TREE_ENTRY,
    KOIN_FILE,
    REPO_ROOT,
    SETTINGS_FILE,
    TEMPLATE_FEATURE,
    block_end,
    relative_to_repo,
)

SCREEN_FILE_SUFFIXES = ["Destination", "Screen", "State", "Event", "Navigation", "ViewModel"]

# The seventh and eighth files live in the test source set rather than beside the other six, and
# they answer different questions: a ViewModel test says what the state becomes, a screen test says
# what is on screen and what a tap does. Neither substitutes for the other, so both are required.
SCREEN_TEST_SUFFIXES = ["ViewModelTest", "ScreenTest"]

# Files in a presentation module that end in `Destination` or `NavGraph` but are not screens.
NOT_A_SCREEN = re.compile(r"(NavGraph)\.kt$")

SUFFIX_TO_LAYER = {
    "Domain": "domain",
    "Data": "data",
    "Presentation": "presentation",
    "Di": "di",
    "Ui": "ui",
}

CHECKS = []


def check(name: str):
    def decorate(function):
        CHECKS.append((name, function))
        return function

    return decorate


def kotlin_files(root: Path):
    if not root.is_dir():
        return
    for path in sorted(root.rglob("*.kt")):
        if "build" in path.relative_to(root).parts:
            continue
        yield path


def build_files(root: Path):
    if not root.is_dir():
        return
    for path in sorted(root.rglob("build.gradle.kts")):
        if "build" in path.relative_to(root).parts:
            continue
        yield path


def feature_names() -> list[str]:
    """Directories under `feature/`, minus any Gradle output that has landed there."""
    root = REPO_ROOT / "feature"
    if not root.is_dir():
        return []
    return sorted(p.name for p in root.iterdir() if p.is_dir() and p.name != "build")


def presentation_dir(feature: str) -> Path:
    return (
        REPO_ROOT / "feature" / feature / "presentation/src/main/kotlin"
        / BASE_PACKAGE.replace(".", "/") / "feature" / feature / "presentation"
    )


def problem(path: Path, line_number: int | None, message: str) -> str:
    location = f"{relative_to_repo(path)}:{line_number}" if line_number else relative_to_repo(path)
    return f"{location}: {message}"


# --------------------------------------------------------------------------------------------
# service/ portability
# --------------------------------------------------------------------------------------------

APP_PROJECT_ACCESSOR = re.compile(r"\bprojects\.(core|feature|app)\b")


@check("service/ depends on nothing app-specific")
def check_service_isolation() -> list[str]:
    problems = []
    for path in build_files(REPO_ROOT / "service"):
        for number, line in enumerate(path.read_text().split("\n"), start=1):
            match = APP_PROJECT_ACCESSOR.search(line)
            if match:
                problems.append(problem(path, number, f"depends on :{match.group(1)} — service/ must stay portable"))
    for path in kotlin_files(REPO_ROOT / "service"):
        for number, line in enumerate(path.read_text().split("\n"), start=1):
            if line.startswith(f"import {BASE_PACKAGE}.feature."):
                problems.append(problem(path, number, "imports a feature — service/ must stay portable"))
    return problems


@check(":service:core:domain is free of the Android framework")
def check_domain_has_no_android() -> list[str]:
    problems = []
    for path in kotlin_files(REPO_ROOT / "service/core/domain"):
        for number, line in enumerate(path.read_text().split("\n"), start=1):
            if re.match(r"import androidx?\.", line):
                problems.append(problem(path, number, f"domain must not import the framework: {line.strip()}"))
    return problems


@check(":service:core:ui resources are prefixed core_")
def check_resource_prefix() -> list[str]:
    module = REPO_ROOT / "service/core/ui"
    if not module.is_dir():
        return []

    problems = []
    build_file = module / "build.gradle.kts"
    if build_file.is_file() and 'resourcePrefix = "core_"' not in build_file.read_text():
        problems.append(problem(build_file, None, 'missing resourcePrefix = "core_"'))

    for path in sorted((module / "src/main/res").rglob("*.xml")) if (module / "src/main/res").is_dir() else []:
        for number, line in enumerate(path.read_text().split("\n"), start=1):
            for name in re.findall(r'\bname="([^"]+)"', line):
                if not name.startswith("core_"):
                    problems.append(problem(path, number, f"resource '{name}' is missing the core_ prefix"))
    return problems


# --------------------------------------------------------------------------------------------
# Screens
# --------------------------------------------------------------------------------------------


@check("every screen is a complete eight-file unit")
def check_screen_units() -> list[str]:
    problems = []
    for feature in feature_names():
        for destination in sorted(presentation_dir(feature).rglob("*Destination.kt")) if presentation_dir(feature).is_dir() else []:
            if NOT_A_SCREEN.search(destination.name):
                continue
            screen = destination.name[: -len("Destination.kt")]
            for suffix in SCREEN_FILE_SUFFIXES:
                sibling = destination.parent / f"{screen}{suffix}.kt"
                if not sibling.is_file():
                    problems.append(problem(destination, None, f"screen '{screen}' is missing {sibling.name}"))

            for suffix in SCREEN_TEST_SUFFIXES:
                test_file = test_dir_for(destination, feature) / f"{screen}{suffix}.kt"
                if not test_file.is_file():
                    problems.append(
                        problem(destination, None, f"screen '{screen}' is missing {test_file.name} — generate it, don't skip it")
                    )
    return problems


def test_dir_for(destination: Path, feature: str) -> Path:
    """The test source set directory mirroring a screen's package, sub-package included."""
    main_root = (
        REPO_ROOT / "feature" / feature / "presentation/src/main/kotlin"
        / BASE_PACKAGE.replace(".", "/") / "feature" / feature / "presentation"
    )
    test_root = (
        REPO_ROOT / "feature" / feature / "presentation/src/test/kotlin"
        / BASE_PACKAGE.replace(".", "/") / "feature" / feature / "presentation"
    )
    return test_root / destination.parent.relative_to(main_root)


@check("every XState has a PREVIEW fixture")
def check_state_previews() -> list[str]:
    problems = []
    for feature in feature_names():
        directory = presentation_dir(feature)
        for state in sorted(directory.rglob("*State.kt")) if directory.is_dir() else []:
            if not re.search(r"\bval PREVIEW\b", state.read_text()):
                problems.append(problem(state, None, "no `val PREVIEW` — it is the preview fixture and the usual initialState"))
    return problems


@check("no ViewModel clears its loading state in an init block")
def check_no_loading_in_init() -> list[str]:
    """
    `BaseViewModel` takes `initialState`, and `execute` drives the overlay. An
    `init { uiState.update { ... loading = null } }` is the pattern that once left a forgotten line
    stranding a screen behind a permanent spinner.

    Subscribing to a flow from `init` and updating `data` is fine — see `SettingsViewModel` — so
    only writes that touch `loading` are flagged.
    """
    problems = []
    for feature in feature_names():
        directory = presentation_dir(feature)
        for view_model in sorted(directory.rglob("*ViewModel.kt")) if directory.is_dir() else []:
            lines = view_model.read_text().split("\n")
            for number, line in enumerate(lines):
                if line.strip() != "init {":
                    continue
                body = "\n".join(lines[number: block_end(lines, number) + 1])
                if re.search(r"uiState\.update\s*\{[^}]*\bloading\s*=", body, re.DOTALL):
                    problems.append(
                        problem(view_model, number + 1, "init block writes `loading` — pass initialState and let execute drive the overlay")
                    )
    return problems


@check("no feature presentation depends on another feature's presentation")
def check_cross_feature_presentation() -> list[str]:
    problems = []
    for feature in feature_names():
        build_file = REPO_ROOT / "feature" / feature / "presentation/build.gradle.kts"
        if not build_file.is_file():
            continue
        for number, line in enumerate(build_file.read_text().split("\n"), start=1):
            match = re.search(r"projects\.feature\.(\w+)\.presentation", line)
            if match and match.group(1) != feature:
                problems.append(
                    problem(build_file, number, f"depends on :feature:{match.group(1)}:presentation — pass a lambda from AppNavHost instead")
                )
    return problems


# --------------------------------------------------------------------------------------------
# Registration
# --------------------------------------------------------------------------------------------

FEATURE_BLOCK = re.compile(r'includeFeatureModule\(\s*"([^"]+)",\s*((?:ModuleSuffix\.\w+,\s*)*)\)')
CORE_BLOCK = re.compile(r"includeCoreModule\(\s*((?:ModuleSuffix\.\w+,\s*)*)\)")
SERVICE_BLOCK = re.compile(r'includeServiceModule\(\s*"([^"]+)",\s*((?:ModuleSuffix\.\w+,\s*)*)\)')


def registered_modules() -> set[str]:
    """Every module path settings.gradle.kts includes, as a repo-relative directory."""
    text = SETTINGS_FILE.read_text()
    paths = set()

    def layers(raw: str) -> list[str]:
        return [SUFFIX_TO_LAYER[s] for s in re.findall(r"ModuleSuffix\.(\w+)", raw) if s in SUFFIX_TO_LAYER]

    for name, raw in FEATURE_BLOCK.findall(text):
        paths.update(f"feature/{name}/{layer}" for layer in layers(raw))
    for name, raw in SERVICE_BLOCK.findall(text):
        paths.update(f"service/{name}/{layer}" for layer in layers(raw))
    for raw in CORE_BLOCK.findall(text):
        paths.update(f"core/{layer}" for layer in layers(raw))
    # Plain includes, for the modules that are not features, services or core layers — `:app`
    # and `:baselineprofile`. Parsed rather than listed, so the next one needs no edit here.
    for path in re.findall(r'include\(\s*"(:[\w:-]+)"\s*\)', text):
        paths.add(path.removeprefix(":").replace(":", "/"))
    # `includeModule(":service:network", "service/network")` — a service that is one module rather
    # than a set of layers, so `includeServiceModule`'s suffixes have nothing to say about it. The
    # interpolated calls inside the helpers do not match: `$` is not in the character class.
    for path in re.findall(r'includeModule\(\s*"(:[\w:-]+)"', text):
        paths.add(path.removeprefix(":").replace(":", "/"))
    return paths


@check("every module on disk is included in settings.gradle.kts")
def check_modules_registered() -> list[str]:
    included = registered_modules()
    problems = []
    for build_file in build_files(REPO_ROOT):
        module = build_file.parent
        # build-logic is an included build, not a module: it is wired in with includeBuild.
        if module == REPO_ROOT or {"buildSrc", "build-logic"} & set(module.parts):
            continue
        relative = relative_to_repo(module)
        if relative not in included:
            problems.append(problem(build_file, None, f"module '{relative}' is not in settings.gradle.kts"))
    return problems


@check("every feature di module is wired into Koin")
def check_koin_registration() -> list[str]:
    if not KOIN_FILE.is_file():
        return [problem(KOIN_FILE, None, "not found")]

    koin = KOIN_FILE.read_text()
    core_di_build = CORE_DI_BUILD_FILE.read_text() if CORE_DI_BUILD_FILE.is_file() else ""
    problems = []

    for feature in feature_names():
        di_dir = (
            REPO_ROOT / "feature" / feature / "di/src/main/kotlin"
            / BASE_PACKAGE.replace(".", "/") / "feature" / feature / "di"
        )
        for module_file in sorted(di_dir.glob("*Module.kt")) if di_dir.is_dir() else []:
            if feature == TEMPLATE_FEATURE:
                continue
            if f"{module_file.stem}.module," not in koin:
                problems.append(problem(KOIN_FILE, None, f"{module_file.stem}.module is not in initKoin()"))
            if f"api(projects.feature.{feature}.di)" not in core_di_build:
                problems.append(problem(CORE_DI_BUILD_FILE, None, f"missing api(projects.feature.{feature}.di)"))
    return problems


@check("every ViewModel is registered in its feature's Koin module")
def check_view_models_registered() -> list[str]:
    problems = []
    for feature in feature_names():
        directory = presentation_dir(feature)
        if not directory.is_dir():
            continue
        di_dir = (
            REPO_ROOT / "feature" / feature / "di/src/main/kotlin"
            / BASE_PACKAGE.replace(".", "/") / "feature" / feature / "di"
        )
        di_text = "\n".join(p.read_text() for p in di_dir.glob("*Module.kt")) if di_dir.is_dir() else ""
        for view_model in sorted(directory.rglob("*ViewModel.kt")):
            if f"viewModelOf(::{view_model.stem})" not in di_text:
                problems.append(problem(view_model, None, f"no viewModelOf(::{view_model.stem}) in feature/{feature}/di"))
    return problems


@check("every feature is listed in CLAUDE.md's module tree")
def check_feature_tree() -> list[str]:
    """
    CLAUDE.md is the rulebook a cold session reads before touching anything, and a module tree
    that has drifted teaches the wrong structure. The generators keep the tree in step; this is
    what makes forgetting visible when a feature is added by hand.
    """
    if not CLAUDE_MD_FILE.is_file():
        return [problem(CLAUDE_MD_FILE, None, "not found")]

    listed = {}
    for number, line in enumerate(CLAUDE_MD_FILE.read_text().split("\n"), start=1):
        match = FEATURE_TREE_ENTRY.match(line)
        if match:
            listed[match.group(1)] = (number, [l for l in match.group(2).split(",") if l])

    on_disk = feature_names()
    problems = []
    for feature in on_disk:
        layers = [l for l in ALL_LAYERS if (REPO_ROOT / "feature" / feature / l).is_dir()]
        if feature not in listed:
            problems.append(problem(CLAUDE_MD_FILE, None, f"feature/{feature} is missing from the module tree"))
            continue
        number, documented = listed[feature]
        if documented != layers:
            problems.append(
                problem(
                    CLAUDE_MD_FILE,
                    number,
                    f"feature/{feature} has {','.join(layers)}; the tree says {','.join(documented)}",
                )
            )
    for feature, (number, _) in listed.items():
        if feature not in on_disk:
            problems.append(problem(CLAUDE_MD_FILE, number, f"lists :feature:{feature}, which does not exist"))
    return problems


@check("every destination is registered in AppNavHost")
def check_destinations_registered() -> list[str]:
    if not APP_NAV_HOST_FILE.is_file():
        return [problem(APP_NAV_HOST_FILE, None, "not found")]

    nav_host = APP_NAV_HOST_FILE.read_text()
    problems = []
    for feature in feature_names():
        # The template feature is compiled but deliberately unreachable.
        if feature == TEMPLATE_FEATURE:
            continue
        directory = presentation_dir(feature)
        for destination in sorted(directory.rglob("*Destination.kt")) if directory.is_dir() else []:
            for function in re.findall(r"fun EntryProviderScope<NavKey>\.(\w+)\(", destination.read_text()):
                if f"{function}(" not in nav_host:
                    problems.append(problem(destination, None, f"{function}() is never called in AppNavHost.kt"))
    return problems


ROUTE_KEY_PARAMETER = re.compile(r"^\s*(?:private val )?\w+: (\w+Destination),?$", re.MULTILINE)


@check("every route key is declared in KoinGraphTest")
def check_route_keys_verified() -> list[str]:
    """
    A screen with navigation arguments takes its route key as a constructor parameter, and the
    destination passes it in with `parametersOf`. Koin's `verify()` cannot know that, so it reports
    the key as a missing definition unless `KoinGraphTest` names it — which is a confusing way to
    find out that a perfectly good screen is fine.
    """
    if not KOIN_GRAPH_TEST_FILE.is_file():
        return [problem(KOIN_GRAPH_TEST_FILE, None, "not found")]

    graph_test = KOIN_GRAPH_TEST_FILE.read_text()
    problems = []
    for feature in feature_names():
        # The template feature is compiled but never registered in the graph.
        if feature == TEMPLATE_FEATURE:
            continue
        directory = presentation_dir(feature)
        for view_model in sorted(directory.rglob("*ViewModel.kt")) if directory.is_dir() else []:
            if not ROUTE_KEY_PARAMETER.search(view_model.read_text()):
                continue
            entry = f"definition<{view_model.stem}>("
            if entry not in graph_test:
                problems.append(
                    problem(view_model, None, f"takes a route key but {entry}...) is not in KoinGraphTest.kt")
                )
    return problems


# --------------------------------------------------------------------------------------------
# Layer direction
# --------------------------------------------------------------------------------------------

# What each layer of a feature is allowed to depend on. The table in CLAUDE.md, enforced.
ALLOWED_FEATURE_DEPENDENCIES = {
    "domain": {"domain"},
    "data": {"domain", "data"},
    "presentation": {"domain", "presentation"},
    "di": {"domain", "data", "presentation", "di"},
}

FEATURE_PROJECT_ACCESSOR = re.compile(r"projects\.feature\.(\w+)\.(\w+)")


@check("feature layers depend only downwards")
def check_layer_direction() -> list[str]:
    """
    `presentation` reaching into `data` compiles perfectly well and quietly undoes the layering —
    the ViewModel ends up talking to a data source instead of a repository. The cross-feature rule
    is checked separately; this one is about layers within a feature.
    """
    problems = []
    for feature in feature_names():
        for layer, allowed in ALLOWED_FEATURE_DEPENDENCIES.items():
            build_file = REPO_ROOT / "feature" / feature / layer / "build.gradle.kts"
            if not build_file.is_file():
                continue
            for number, line in enumerate(build_file.read_text().split("\n"), start=1):
                match = FEATURE_PROJECT_ACCESSOR.search(line)
                if not match:
                    continue
                target_feature, target_layer = match.group(1), match.group(2)
                if target_feature != feature:
                    continue  # cross-feature deps are check_cross_feature_presentation's business
                if target_layer not in allowed:
                    problems.append(
                        problem(
                            build_file,
                            number,
                            f"{layer} must not depend on {target_layer} — allowed: {', '.join(sorted(allowed))}",
                        )
                    )
    return problems


DEFAULT_DATA_SOURCE_IMPORT = re.compile(r"^import [\w.]*\.(Default\w*DataSource)$", re.MULTILINE)


@check("no repository imports a data source implementation")
def check_repository_depends_on_interface() -> list[str]:
    """
    The rule the `gateway` module used to enforce by living in a different module. Now that both
    halves sit in `:feature:x:data`, nothing but this stops `DefaultXRepository` from constructing
    a `DefaultXDataSource` directly — and a repository wired to an implementation cannot have its
    source swapped for a cache or a fake, which is the whole point of the layer.
    """
    problems = []
    for feature in feature_names():
        data_dir = REPO_ROOT / "feature" / feature / "data"
        for path in kotlin_files(data_dir):
            if not path.stem.endswith("Repository"):
                continue
            for number, line in enumerate(path.read_text().split("\n"), start=1):
                match = DEFAULT_DATA_SOURCE_IMPORT.match(line)
                if match:
                    interface = match.group(1)[len("Default"):]
                    problems.append(
                        problem(path, number, f"imports {match.group(1)} — depend on {interface} instead")
                    )
    return problems


SERVICE_LAYER_ALLOWED = {"domain": set(), "data": {"domain"}, "ui": {"domain"}}


@check("service layers depend only downwards")
def check_service_layer_direction() -> list[str]:
    problems = []
    for module in sorted((REPO_ROOT / "service").glob("*/*")) if (REPO_ROOT / "service").is_dir() else []:
        build_file = module / "build.gradle.kts"
        allowed = SERVICE_LAYER_ALLOWED.get(module.name)
        if not build_file.is_file() or allowed is None:
            continue
        for number, line in enumerate(build_file.read_text().split("\n"), start=1):
            match = re.search(r"projects\.service\.\w+\.(\w+)", line)
            if match and match.group(1) not in allowed:
                problems.append(
                    problem(build_file, number, f"service {module.name} must not depend on {match.group(1)}")
                )
    return problems


# --------------------------------------------------------------------------------------------
# Compose conventions
# --------------------------------------------------------------------------------------------

PUBLIC_COMPOSABLE = re.compile(r"^(?:@\w+(?:\([^)]*\))?\s*)*fun ([A-Z]\w*)\s*\(")


@check("every public composable takes a Modifier parameter")
def check_modifier_parameter() -> list[str]:
    """
    The first rule in Compose's own API guidelines: a composable that emits UI takes
    `modifier: Modifier = Modifier` so its caller can position it. Previews and screen-level
    composables that fill the window are exempt, as is everything outside `src/main`.
    """
    problems = []
    roots = [REPO_ROOT / "core/ui", REPO_ROOT / "service/core/ui"]
    roots += [REPO_ROOT / "feature" / f / "presentation" for f in feature_names()]

    for root in roots:
        for path in kotlin_files(root):
            # Production composables only. A preview in `screenshotTest` positions nothing — it is
            # the thing being rendered.
            if "/src/main/" not in path.as_posix():
                continue
            text = path.read_text()
            if "@Composable" not in text:
                continue
            lines = text.split("\n")
            for number, line in enumerate(lines):
                if line.strip() != "@Composable":
                    continue
                # find the fun declaration that follows the annotation block
                index = number + 1
                while index < len(lines) and not lines[index].lstrip().startswith("fun "):
                    if lines[index].lstrip().startswith("private fun ") or lines[index].strip() == "":
                        break
                    index += 1
                if index >= len(lines):
                    continue
                declaration = lines[index].lstrip()
                if not declaration.startswith("fun "):
                    continue
                name = declaration[4:].split("(")[0].split("<")[0].strip()
                # An extension (`fun UiText.resolve()`) is a helper, not something that emits UI.
                if "." in name or not name[:1].isupper():
                    continue
                body = "\n".join(lines[index:index + 60])
                # Match parens rather than splitting on the first ")": a parameter list is full of
                # them (`onConfirm: () -> Unit`), and splitting truncates before `modifier`.
                start = body.index("(")
                depth, end = 0, start
                for position in range(start, len(body)):
                    depth += (body[position] == "(") - (body[position] == ")")
                    if depth == 0:
                        end = position
                        break
                signature = body[start:end + 1]
                returns_value = re.match(r"\s*:\s*(?!Unit\b)\w", body[end + 1:])
                # Exempt what has nothing to position: a value-returning helper, a theme or
                # CompositionLocal wrapper, a preview, and a screen-level composable that fills
                # the window it is given.
                if (
                    returns_value
                    or name.endswith(("Screen", "Preview", "Theme", "Provider"))
                    # Compose's own idiom for a CompositionLocal wrapper — `ProvideTextStyle`.
                    or name.startswith("Provide")
                ):
                    continue
                if "modifier: Modifier" not in signature:
                    problems.append(
                        problem(path, index + 1, f"composable '{name}' takes no `modifier: Modifier = Modifier`")
                    )
    return problems


# --------------------------------------------------------------------------------------------
# Design system
# --------------------------------------------------------------------------------------------

# A feature composes components from :core:ui; it never draws one. These are what "never draws"
# means in grep terms — see CLAUDE.md's Design system section for the reasoning.
MATERIAL_IMPORT = re.compile(r"^import androidx\.compose\.material3\.(\w+)", re.MULTILINE)
DIMENSION_LITERAL = re.compile(r"(?<![\w.])\d+(?:\.\d+)?\.(dp|sp)\b")
RAW_COLOR = re.compile(r"\bColor\(0x|\bMaterialTheme\.colorScheme\b")
# Swapping the image loader should be a change to AppImage and nothing else.
IMAGE_LIBRARY = re.compile(r"^import coil3?\.", re.MULTILINE)

# A number a person reads has a role, the same way a colour does — see core.10. What this catches
# is the two copies of `Price.kt` that grew before there was one: a feature formatting its own
# money, and doing it slightly differently in each place.
NUMBER_FORMATTER = re.compile(
    r"^import (java\.text\.(?:NumberFormat|DecimalFormat)|java\.time\.format\.DateTimeFormatter)$",
    re.MULTILINE,
)

# Material types a screen legitimately names because they are types, not widgets: they appear in
# a component's own signature and a feature has to spell them to call it.
MATERIAL_TYPE_ALLOWLIST = {
    "ExperimentalMaterial3Api",
}


@check("no feature draws its own UI")
def check_features_use_the_design_system() -> list[str]:
    problems = []
    for feature in feature_names():
        root = REPO_ROOT / "feature" / feature / "presentation/src/main"
        for path in kotlin_files(root):
            text = path.read_text()
            for match in MATERIAL_IMPORT.finditer(text):
                if match.group(1) in MATERIAL_TYPE_ALLOWLIST:
                    continue
                line = text[: match.start()].count("\n") + 1
                problems.append(
                    problem(
                        path,
                        line,
                        f"imports Material's {match.group(1)} — compose the :core:ui component "
                        "instead, or add one with create_component.py",
                    )
                )
            for match in DIMENSION_LITERAL.finditer(text):
                line = text[: match.start()].count("\n") + 1
                problems.append(
                    problem(
                        path,
                        line,
                        f"has a bare `{match.group(0)}` — ask AppTheme.spacing, "
                        "AppTheme.typography or AppTheme.icons for a role",
                    )
                )
            for match in IMAGE_LIBRARY.finditer(text):
                line = text[: match.start()].count("\n") + 1
                problems.append(
                    problem(
                        path,
                        line,
                        "imports the image library directly — use AppImage, which is the one "
                        "place that knows it exists",
                    )
                )
            for match in RAW_COLOR.finditer(text):
                line = text[: match.start()].count("\n") + 1
                problems.append(
                    problem(path, line, "names a colour directly — ask AppTheme.colors for a role")
                )
            for match in NUMBER_FORMATTER.finditer(text):
                line = text[: match.start()].count("\n") + 1
                problems.append(
                    problem(
                        path,
                        line,
                        f"formats a number itself with {match.group(1).rsplit('.', 1)[1]} — ask "
                        "LocalFormats.current for a role (money, weight, percent, date, ...)",
                    )
                )
    return problems


@check("every XState is @Immutable")
def check_state_immutability() -> list[str]:
    """Strong skipping covers most of it, but the annotation is what documents the intent.

    A state holds lists — `List<Product>` is an unstable type to the compiler, so without this the
    screen recomposes on every parent recomposition whether or not anything it shows has changed.
    """
    problems = []
    for feature in feature_names():
        for path in kotlin_files(presentation_dir(feature)):
            if not path.name.endswith("State.kt"):
                continue
            text = path.read_text()
            declaration = re.search(r"^data class (\w+State)", text, re.MULTILINE)
            if declaration and "@Immutable" not in text:
                problems.append(
                    problem(path, None, f"`{declaration.group(1)}` is not annotated @Immutable")
                )
    return problems


@check("every :core:ui component has a preview")
def check_component_previews() -> list[str]:
    """A component nobody can look at is a component nobody trusts.

    The preview is also what the gallery and item 4.1's screenshot tests are built on, so a
    missing one is a hole in both.
    """
    problems = []
    root = REPO_ROOT / "core/ui/src/main/kotlin" / BASE_PACKAGE.replace(".", "/") / "core/ui/component"
    for path in kotlin_files(root):
        text = path.read_text()
        if "@ComponentPreview" not in text:
            problems.append(problem(path, None, "has no @ComponentPreview"))
    return problems


# --------------------------------------------------------------------------------------------
# Provenance
# --------------------------------------------------------------------------------------------

# Identifiers that must never appear in this repo. This template's architecture evolved from a
# client codebase; a name carried in by a hurried copy-paste is the way that becomes a problem.
FOREIGN_IDENTIFIERS = [
    "mcedison",
    "prometheus",
    "com.mobile.plugin",
]


@check("no foreign project identifiers")
def check_foreign_identifiers() -> list[str]:
    problems = []
    skip_dirs = {".git", "build", ".gradle", "__pycache__", ".idea", ".kotlin"}
    for path in sorted(REPO_ROOT.rglob("*")):
        if not path.is_file() or path.suffix not in {".kt", ".kts", ".py", ".xml", ".toml", ".md", ".yml", ".pro"}:
            continue
        if set(path.relative_to(REPO_ROOT).parts) & skip_dirs:
            continue
        if path.name == "doctor.py":
            continue  # this list lives here
        lowered = path.read_text(errors="ignore").lower()
        for identifier in FOREIGN_IDENTIFIERS:
            if identifier in lowered:
                problems.append(problem(path, None, f"contains '{identifier}' — see the provenance note in README.md"))
    return problems


# --------------------------------------------------------------------------------------------
# Build files
# --------------------------------------------------------------------------------------------

HARDCODED_COORDINATE = re.compile(
    r"^\s*(?:api|implementation|compileOnly|runtimeOnly|testImplementation|androidTestImplementation|debugImplementation)"
    r"\s*\(\s*(?:platform\(\s*)?\"[\w.\-]+:[\w.\-]+"
)


# Configuration a module must not set for itself: it belongs to a convention plugin in
# build-logic/, and 24 copies of it were the reason `minSdk` used to be 24 edits.
SHARED_ANDROID_CONFIG = re.compile(r"^\s*(compileSdk|minSdk|targetSdk|compileOptions|lint)\b\s*[({=]")

# resourcePrefix and testFixtures are genuinely per module; :service:core:ui sets both.
MODULE_OWNED_ANDROID_CONFIG = {"resourcePrefix", "testFixtures"}


@check("no module build file repeats the shared Android configuration")
def check_no_duplicated_android_config() -> list[str]:
    """
    The convention plugins only pay for themselves while the modules stay thin: one build file that
    sets its own `compileSdk` drifts silently, and the next `minSdk` change misses it.
    """
    problems = []
    for path in build_files(REPO_ROOT):
        if path.parent == REPO_ROOT or "build-logic" in path.parts:
            continue
        for number, line in enumerate(path.read_text().split("\n"), start=1):
            match = SHARED_ANDROID_CONFIG.match(line)
            if match and match.group(1) not in MODULE_OWNED_ANDROID_CONFIG:
                problems.append(
                    problem(path, number, f"sets {match.group(1)} — that belongs to a build-logic convention plugin")
                )
    return problems


@check("no dependency version is hardcoded outside the version catalog")
def check_no_hardcoded_versions() -> list[str]:
    problems = []
    for path in build_files(REPO_ROOT):
        for number, line in enumerate(path.read_text().split("\n"), start=1):
            if HARDCODED_COORDINATE.match(line):
                problems.append(problem(path, number, f"hardcoded coordinate: {line.strip()} — use libs.versions.toml"))
    return problems


# --------------------------------------------------------------------------------------------
# Presentation layout (D34)
# --------------------------------------------------------------------------------------------

PREVIEW_ANNOTATIONS = {"@ScreenPreview", "@ComponentPreview", "@Preview"}


def screen_destinations(feature: str):
    """`(destination file, screen stem)` for every screen in a feature's presentation module."""
    directory = presentation_dir(feature)
    if not directory.is_dir():
        return
    for destination in sorted(directory.rglob("*Destination.kt")):
        if NOT_A_SCREEN.search(destination.name):
            continue
        yield destination, destination.name[: -len("Destination.kt")]


@check("every screen has a directory of its own, holding nothing else")
def check_screen_directories() -> list[str]:
    """
    D34: a screen's eight-file unit lives in a sub-package named after it, even when the feature
    has only one screen — so a second screen never forces a move, and every feature reads the same.

    What the flat package cost was concrete: the catalog's four screens shared a directory of 24
    files, and a feature's own composable had nowhere to live but the screen file it was written
    in. The rule only holds while nothing else moves in beside a screen, which is what this
    checks: the directory holds that screen's six files and not one more.
    """
    problems = []
    for feature in feature_names():
        root = presentation_dir(feature)
        for destination, screen in screen_destinations(feature):
            directory = destination.parent
            if directory == root:
                problems.append(
                    problem(destination, None, f"screen '{screen}' sits in the presentation package root — give it a directory, as create_screen.py does")
                )
                continue
            own = {f"{screen}{suffix}.kt" for suffix in SCREEN_FILE_SUFFIXES}
            for path in sorted(directory.glob("*.kt")):
                if path.name not in own:
                    problems.append(
                        problem(path, None, f"is not part of screen '{screen}' — a screen's directory holds its own unit and nothing else; a composable goes to the feature's component/")
                    )
            for path in sorted(p for p in directory.iterdir() if p.is_dir()):
                problems.append(
                    problem(destination, None, f"screen '{screen}' has a nested directory '{path.name}' — a screen's directory holds its own unit and nothing else")
                )
    return problems


def top_level_composables(text: str) -> list[tuple[str, set[str], int]]:
    """`(name, annotations, line number)` for every top-level `@Composable fun` in a file."""
    found = []
    annotations: set[str] = set()
    for number, line in enumerate(text.split("\n"), start=1):
        if line.startswith("@"):
            annotations.add(line.split("(")[0].rstrip())
            continue
        match = re.match(r"(?:public |internal |private )?fun ([A-Za-z_]\w*)\s*[(<]", line)
        if match and "@Composable" in annotations:
            found.append((match.group(1), annotations, number))
        annotations = set()
    return found


@check("a screen file holds no composable but the screen and its previews")
def check_screen_files_hold_only_the_screen() -> list[str]:
    """
    D34's other half. `ProfileScreen.kt` had grown seven composables and a `FileProvider` helper,
    which is how a feature's own component ends up unpreviewable and unreachable from a second
    screen. Anything that is not the screen goes to the feature's `component/`, one to a file with
    a `@ComponentPreview`; a component a second feature wants goes to `:core:ui` through
    `create_component.py`.
    """
    problems = []
    for feature in feature_names():
        for destination, screen in screen_destinations(feature):
            for path in sorted(destination.parent.glob("*.kt")):
                for name, annotations, number in top_level_composables(path.read_text()):
                    if name == f"{screen}Screen" or annotations & PREVIEW_ANNOTATIONS:
                        continue
                    problems.append(
                        problem(path, number, f"composable '{name}' is not the screen — move it to feature/{feature}/presentation/.../component/ with create_component.py --feature {feature}")
                    )
    return problems



# --------------------------------------------------------------------------------------------


def main() -> None:
    parser = argparse.ArgumentParser(
        description="Check this project's architectural conventions.",
        epilog=(
            'Examples:\n'
            '  python3 scripts/doctor.py\n'
            '  python3 scripts/doctor.py --list\n'
            '\n'
            'Exits non-zero, so it gates CI and the pre-commit hook. If a check looks wrong, fix the check\n'
            'rather than working around it — each one exists because a compiler cannot catch it.'
        ),
        formatter_class=argparse.RawDescriptionHelpFormatter,
    )
    parser.add_argument("--list", action="store_true", help="List the checks and exit.")
    args = parser.parse_args()

    if args.list:
        for name, _ in CHECKS:
            print(f"  {name}")
        return

    failed = 0
    for name, function in CHECKS:
        problems = function()
        if problems:
            failed += 1
            print(f"[FAIL] {name}")
            for line in problems:
                print(f"         {line}")
        else:
            print(f"[ ok ] {name}")

    print()
    if failed:
        print(f"{failed} of {len(CHECKS)} checks failed.")
        sys.exit(1)
    print(f"All {len(CHECKS)} checks passed.")


if __name__ == "__main__":
    main()
