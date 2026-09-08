"""Shared helpers for the scaffolding scripts."""

from __future__ import annotations

import re
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parent.parent

# Base package of the app. Change this (and the directories on disk) if you rename the package.
BASE_PACKAGE = "com.example.androidproject1"
BASE_PATH = BASE_PACKAGE.replace(".", "/")

TEMPLATE_FEATURE = "template"
TEMPLATE_CLASS = "Template"

# String resources in the template are named `template_*`; generated code renames the prefix so that
# two screens in one feature cannot collide on `template_title`.
TEMPLATE_RESOURCE_PREFIX = "template"

# Where new features are inserted in settings.gradle.kts: the template block stays last.
TEMPLATE_ANCHOR = f'includeFeatureModule(\n    "{TEMPLATE_FEATURE}",'

SETTINGS_FILE = REPO_ROOT / "settings.gradle.kts"
CLAUDE_MD_FILE = REPO_ROOT / "CLAUDE.md"
KOIN_FILE = REPO_ROOT / "core/di/src/main/kotlin" / BASE_PATH / "core/di/Koin.kt"
KOIN_GRAPH_TEST_FILE = REPO_ROOT / "app/src/test/kotlin" / BASE_PATH / "KoinGraphTest.kt"
CORE_DI_BUILD_FILE = REPO_ROOT / "core/di/build.gradle.kts"
APP_NAV_HOST_FILE = REPO_ROOT / "app/src/main/kotlin" / BASE_PATH / "AppNavHost.kt"
VERSION_CATALOG_FILE = REPO_ROOT / "gradle/libs.versions.toml"

# Order matters: this is also the order layers are listed in settings.gradle.kts.
ALL_LAYERS = ["domain", "data", "presentation", "di"]

LAYER_SUFFIX = {
    "domain": "Domain",
    "data": "Data",
    "presentation": "Presentation",
    "di": "Di",
}

# The entry blocks in AppNavHost.kt, keyed by the `--graph` value the scripts accept. Navigation 3
# has no nested graphs, so these are grouping functions rather than framework objects — but they are
# still where a generated destination belongs, and still the thing `--graph` chooses between.
NAV_GRAPHS = {
    "main": "mainEntries",
    "auth": "authEntries",
}

STRINGS_XML_TEMPLATE = '<?xml version="1.0" encoding="utf-8"?>\n<resources>\n</resources>\n'

# A line of the module tree in CLAUDE.md, e.g.
# `:feature:auth:{domain,data,presentation,di}       full stack; owns the session`.
# doctor.py fails when the tree and the directories on disk disagree, so the generators keep it
# in step — it is the fifth registration, and the only one a compiler could never catch.
FEATURE_TREE_ENTRY = re.compile(r"^:feature:(\w+):\{([\w,]*)\}(?:\s+(.*))?$")

# Column the descriptions in that tree start at.
FEATURE_TREE_COLUMN = 50


# --------------------------------------------------------------------------------------------
# Naming
# --------------------------------------------------------------------------------------------


def split_words(name: str) -> list[str]:
    """Splits `userProfile`, `user_profile`, `user-profile` and `UserProfile` into ['user', 'profile']."""
    spaced = re.sub(r"[_\-\s]+", " ", name)
    spaced = re.sub(r"(?<=[a-z0-9])(?=[A-Z])", " ", spaced)
    return [w.lower() for w in spaced.split() if w]


def to_flat(name: str) -> str:
    """`userProfile` -> `userprofile`. Used for package and directory names."""
    return "".join(split_words(name))


def to_pascal(name: str) -> str:
    """`userProfile` -> `UserProfile`. Used for class names."""
    return "".join(w.capitalize() for w in split_words(name))


def to_camel(name: str) -> str:
    """`UserProfile` -> `userProfile`. Used for function names."""
    pascal = to_pascal(name)
    return pascal[:1].lower() + pascal[1:]


def to_snake(name: str) -> str:
    """`UserProfile` -> `user_profile`. Used for resource name prefixes."""
    return "_".join(split_words(name))


# --------------------------------------------------------------------------------------------
# Source rewriting
# --------------------------------------------------------------------------------------------


def rewrite_source(text: str, flat: str, pascal: str, camel: str) -> str:
    """
    Rewrites template identifiers to the target feature's.

    Targeted rather than a blanket `template` -> `<name>` so that the word appearing in a comment
    or a string is left alone.

    Two spellings of the name, and the difference matters: packages and directories are flat
    lowercase (`feature.userprofile`), Kotlin identifiers are camelCase (`userProfileDestination`),
    the same spelling `create_screen.py` produces.
    """
    text = text.replace(f"feature.{TEMPLATE_FEATURE}", f"feature.{flat}")
    text = text.replace(f"feature/{TEMPLATE_FEATURE}", f"feature/{flat}")
    # camelCase identifiers such as `templateDestination`.
    text = re.sub(rf"\b{TEMPLATE_FEATURE}(?=[A-Z])", camel, text)
    text = text.replace(TEMPLATE_CLASS, pascal)
    return text


def rewrite_resource_names(text: str, prefix: str) -> str:
    """
    Renames `template_foo` to `<prefix>_foo`, in both `R.string.template_foo` and `name="template_foo"`.

    `rewrite_source` cannot: its camelCase rule needs an uppercase letter after `template`, and
    resource names are snake_case. Skipping this leaves a generated screen pointing at a string that
    exists only in the template feature.
    """
    return re.sub(rf"\b{TEMPLATE_RESOURCE_PREFIX}_(\w+)", rf"{prefix}_\1", text)


def rewrite_relative_path(relative: Path, flat: str, pascal: str) -> Path:
    """
    Maps a path inside the template module to the generated module. Only the
    `.../feature/template/...` package segment is rewritten.
    """
    as_posix = relative.as_posix()
    as_posix = as_posix.replace(
        f"{BASE_PATH}/feature/{TEMPLATE_FEATURE}/",
        f"{BASE_PATH}/feature/{flat}/",
    )
    parts = as_posix.split("/")
    parts[-1] = parts[-1].replace(TEMPLATE_CLASS, pascal)
    return Path("/".join(parts))


# --------------------------------------------------------------------------------------------
# File I/O
# --------------------------------------------------------------------------------------------


def relative_to_repo(path: Path) -> str:
    try:
        return str(path.relative_to(REPO_ROOT))
    except ValueError:
        return str(path)


def write_file(path: Path, content: str, dry_run: bool) -> None:
    if dry_run:
        print(f"  would write {relative_to_repo(path)}")
        return
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(content)
    print(f"  wrote {relative_to_repo(path)}")


def edit_file(path: Path, transform, dry_run: bool, label: str) -> bool:
    """Applies `transform` to a file's text. Returns True if it changed anything."""
    if not path.is_file():
        print(f"  {label}: {relative_to_repo(path)} not found, skipped")
        return False
    original = path.read_text()
    updated = transform(original)
    if updated == original:
        print(f"  {label}: no change needed")
        return False
    if dry_run:
        print(f"  would update {relative_to_repo(path)} ({label})")
        return True
    path.write_text(updated)
    print(f"  updated {relative_to_repo(path)} ({label})")
    return True


# --------------------------------------------------------------------------------------------
# Module paths
# --------------------------------------------------------------------------------------------


def feature_module_dir(flat: str, layer: str) -> Path:
    return REPO_ROOT / "feature" / flat / layer


def feature_source_dir(flat: str, layer: str, sub: str = "") -> Path:
    """
    The package directory holding a layer's Kotlin sources.

    `sub` names a package inside the layer — `data` splits into `repository` and `source`.
    """
    directory = feature_module_dir(flat, layer) / "src/main/kotlin" / BASE_PATH / "feature" / flat / layer
    return directory / sub if sub else directory


def feature_package(flat: str, layer: str, sub: str = "") -> str:
    package = f"{BASE_PACKAGE}.feature.{flat}.{layer}"
    return f"{package}.{sub}" if sub else package


def module_namespace(module_dir: Path, fallback: str) -> str:
    """
    The Android namespace a module's `R` lives in.

    Since the convention plugins landed, almost every build file leaves `namespace` unset and the
    plugin derives it from the project path — which is what `fallback` is. An explicit `namespace`
    still wins, both in Gradle and here.
    """
    build_file = module_dir / "build.gradle.kts"
    if build_file.is_file():
        match = re.search(r'namespace\s*=\s*"([^"]+)"', build_file.read_text())
        if match:
            return match.group(1)
    return fallback


def feature_koin_module_file(flat: str) -> Path | None:
    """The feature's `XModule.kt`, or None if the feature has no `di` layer yet."""
    di_dir = feature_source_dir(flat, "di")
    modules = sorted(di_dir.glob("*Module.kt")) if di_dir.is_dir() else []
    return modules[0] if modules else None


# --------------------------------------------------------------------------------------------
# Kotlin source edits
# --------------------------------------------------------------------------------------------


def insert_import(lines: list[str], import_line: str) -> None:
    """Inserts an import into an already sorted import block, in place."""
    if import_line in lines:
        return
    import_indexes = [i for i, line in enumerate(lines) if line.startswith("import ")]
    insert_at = next(
        (i for i in import_indexes if lines[i] > import_line),
        import_indexes[-1] + 1 if import_indexes else 0,
    )
    lines.insert(insert_at, import_line)


def block_end(lines: list[str], start: int) -> int:
    """Index of the line closing the brace opened on `lines[start]`."""
    depth = 0
    for i in range(start, len(lines)):
        depth += lines[i].count("{") - lines[i].count("}")
        if depth <= 0 and i > start:
            return i
    raise ValueError(f"unbalanced braces from line {start + 1}")


def insert_after_last(lines: list[str], pattern: re.Pattern[str], entry: str, fallback: re.Pattern[str], fallback_indent: str) -> None:
    """
    Inserts `entry` after the last line matching `pattern`, keeping that line's indent.

    Falls back to inserting after the first line matching `fallback` (e.g. the opening `module {`)
    when there is no existing entry to anchor to.
    """
    matches = [i for i, line in enumerate(lines) if pattern.match(line)]
    if matches:
        anchor = matches[-1]
        indent = pattern.match(lines[anchor]).group(1)
    else:
        anchor = next(i for i, line in enumerate(lines) if fallback.match(line))
        indent = fallback_indent
    lines.insert(anchor + 1, f"{indent}{entry}")


def remove_lines(text: str, predicate) -> str:
    """Drops every line for which `predicate(line)` is true."""
    return "\n".join(line for line in text.split("\n") if not predicate(line))


# --------------------------------------------------------------------------------------------
# String resources
# --------------------------------------------------------------------------------------------

_STRING_ENTRY = re.compile(r'<string\s+name="([^"]+)"\s*>(.*?)</string>', re.DOTALL)


def read_string_resources(path: Path) -> dict[str, str]:
    if not path.is_file():
        return {}
    return {name: value for name, value in _STRING_ENTRY.findall(path.read_text())}


def merge_strings_xml(path: Path, entries: dict[str, str], dry_run: bool) -> None:
    """Adds `entries` to a `strings.xml`, creating it if needed and skipping names already there."""
    if not entries:
        return

    existing = read_string_resources(path)
    missing = {name: value for name, value in entries.items() if name not in existing}
    if not missing:
        print(f"  strings: {relative_to_repo(path)} already has {', '.join(entries)}")
        return

    text = path.read_text() if path.is_file() else STRINGS_XML_TEMPLATE
    block = "".join(f'    <string name="{name}">{value}</string>\n' for name, value in missing.items())
    if "</resources>" in text:
        text = text.replace("</resources>", block + "</resources>", 1)
    else:
        text = text.rstrip("\n") + "\n" + block

    write_file(path, text, dry_run)


# --------------------------------------------------------------------------------------------
# AppNavHost registration
# --------------------------------------------------------------------------------------------


def register_destination(import_line: str, call_line: str, graph: str, dry_run: bool) -> None:
    """Adds `xDestination(backStack = backStack)` to an entry block in AppNavHost.kt."""
    if graph == "none":
        print("  AppNavHost: --graph none, registration skipped")
        return

    graph_function = NAV_GRAPHS[graph]
    call_name = call_line.split("(")[0]

    def transform(text: str) -> str:
        lines = text.split("\n")
        if any(line.strip().startswith(f"{call_name}(") for line in lines):
            return text

        anchor = f"fun EntryProviderScope<NavKey>.{graph_function}("
        start = next((i for i, line in enumerate(lines) if anchor in line), None)
        if start is None:
            print(f"  AppNavHost: no {graph_function}() block — add the destination by hand")
            return text

        end = block_end(lines, start)
        indent = re.match(r"\s*", lines[start]).group(0) + " " * 4
        # A blank line between destinations only if the graph already has one.
        if lines[end - 1].strip():
            lines.insert(end, f"{indent}{call_line}")
        else:
            lines.insert(end - 1, f"{indent}{call_line}")
        insert_import(lines, import_line)
        return "\n".join(lines)

    edit_file(APP_NAV_HOST_FILE, transform, dry_run, f"register destination in {graph_function}()")


def register_route_key_injection(feature: str, pascal: str, sub_package: str, dry_run: bool) -> None:
    """
    Teaches `KoinGraphTest` that a screen's route key comes from `parametersOf`, not the graph.

    Only a screen with navigation arguments needs this. Without it `verify()` reports the route key
    as a missing definition, which is a confusing way to learn that a generated screen is fine.
    """
    package_suffix = f".{sub_package}" if sub_package else ""
    package = f"{BASE_PACKAGE}.feature.{feature}.presentation{package_suffix}"
    entry = f"                definition<{pascal}ViewModel>({pascal}Destination::class),"

    def transform(text: str) -> str:
        if entry.strip() in text:
            return text
        lines = text.split("\n")
        anchor = next((i for i, line in enumerate(lines) if "injectedParameters(" in line), None)
        if anchor is None:
            print("  KoinGraphTest: no injectedParameters(...) block — add the route key by hand")
            return text
        lines.insert(block_end(lines, anchor), entry)
        insert_import(lines, f"import {package}.{pascal}Destination")
        insert_import(lines, f"import {package}.{pascal}ViewModel")
        return "\n".join(lines)

    edit_file(KOIN_GRAPH_TEST_FILE, transform, dry_run, "register the route key in KoinGraphTest")


# --------------------------------------------------------------------------------------------
# CLAUDE.md module tree
# --------------------------------------------------------------------------------------------


def feature_tree_line(flat: str, layers: list[str], description: str) -> str:
    path = f":feature:{flat}:{{{','.join(layers)}}}"
    if not description:
        return path
    return f"{path}{' ' * max(2, FEATURE_TREE_COLUMN - len(path))}{description}"


def _tree_entries(lines: list[str]) -> list[tuple[int, str]]:
    """`(line index, feature name)` for every module-tree entry, in the order they appear."""
    return [(i, m.group(1)) for i, line in enumerate(lines) if (m := FEATURE_TREE_ENTRY.match(line))]


def register_in_feature_tree(flat: str, layers: list[str], description: str, dry_run: bool) -> None:
    """Lists the feature in CLAUDE.md's module tree, alphabetically, with the template last."""
    line = feature_tree_line(flat, layers, description)

    def transform(text: str) -> str:
        lines = text.split("\n")
        entries = _tree_entries(lines)
        if not entries:
            print("  CLAUDE.md: no module tree found — list the feature by hand")
            return text

        existing = next((i for i, name in entries if name == flat), None)
        if existing is not None:
            # A second `--layers ... --force` run grows the feature; the tree grows with it.
            lines[existing] = line
            return "\n".join(lines)

        others = [(i, name) for i, name in entries if name != TEMPLATE_FEATURE]
        after = next((i for i, name in others if name > flat), None)
        if after is None:
            after = others[-1][0] + 1 if others else entries[0][0]
        lines.insert(after, line)
        return "\n".join(lines)

    edit_file(CLAUDE_MD_FILE, transform, dry_run, "list the module in CLAUDE.md")


def unregister_from_feature_tree(flat: str, dry_run: bool) -> None:
    def transform(text: str) -> str:
        lines = text.split("\n")
        drop = {i for i, name in _tree_entries(lines) if name == flat}
        return "\n".join(line for i, line in enumerate(lines) if i not in drop)

    edit_file(CLAUDE_MD_FILE, transform, dry_run, "remove the module from CLAUDE.md")
