#!/usr/bin/env python3
"""
Scaffolds a new feature module from `feature/template`.

    python3 scripts/create_feature.py userProfile
    python3 scripts/create_feature.py userProfile --layers presentation,di
    python3 scripts/create_feature.py userProfile --graph auth
    python3 scripts/create_feature.py userProfile --dry-run

Unlike a plain copy, this also performs every registration step the new module needs:
`settings.gradle.kts`, `:core:di`'s build file, the Koin module list, `AppNavHost.kt` and the
module tree in `CLAUDE.md`.
"""

from __future__ import annotations

import argparse
import re
import shutil
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))

from _common import (  # noqa: E402
    ALL_LAYERS,
    BASE_PACKAGE,
    CORE_DI_BUILD_FILE,
    KOIN_FILE,
    LAYER_SUFFIX,
    NAV_GRAPHS,
    REPO_ROOT,
    SETTINGS_FILE,
    TEMPLATE_ANCHOR,
    TEMPLATE_FEATURE,
    edit_file,
    insert_import,
    register_destination,
    register_in_feature_tree,
    rewrite_relative_path,
    rewrite_resource_names,
    rewrite_source,
    rewrite_test_tags,
    to_camel,
    to_flat,
    to_pascal,
    to_snake,
    write_file,
)

TEXT_SUFFIXES = {".kt", ".kts", ".xml", ".pro"}

# The list in Koin.kt a new feature joins. Koin.kt holds a second one, `debugMenuModules()`,
# for the features only a dev or staging build registers (D16); a new feature is not one.
KOIN_MODULE_LIST = "appModules"

# Generated or IDE-local directories that must never be cloned into a new module. `screenshots`
# holds the template's own Roborazzi goldens (ui.2): the generated feature's previews are named
# after it, so the clone would carry a set of images that no test of its own ever looks at.
# `recordRoborazziDebug` writes the right ones.
EXCLUDED_DIRS = {"build", ".gradle", ".idea", ".cxx", "screenshots"}
EXCLUDED_FILES = {".DS_Store"}

# The template feature holds two screens: the plain one and the `TemplateArgs` variant that
# `create_screen.py --with-args` clones. A new feature starts with one screen, so the args set is
# skipped here — copying it would leave every generated feature with a second, unregistered
# destination that doctor.py rightly fails on.
EXCLUDED_PREFIX = "TemplateArgs"
EXCLUDED_RESOURCE_PREFIX = "template_args_"


def is_copyable(relative: Path) -> bool:
    if EXCLUDED_DIRS.intersection(relative.parts):
        return False
    if relative.name.startswith(EXCLUDED_PREFIX):
        return False
    return relative.name not in EXCLUDED_FILES


def strip_args_strings(text: str) -> str:
    """Drops the args screen's string resources, whose screen is not being copied."""
    return "\n".join(
        line for line in text.split("\n") if f'name="{EXCLUDED_RESOURCE_PREFIX}' not in line
    )


def strip_args_registration(text: str) -> str:
    """Drops the args screen's Koin lines.

    `is_copyable` skips the args screen by file name, which leaves the di module — one file
    naming both ViewModels — importing and registering one that was never copied. Without this
    the generated feature does not compile.
    """
    return "\n".join(
        line for line in text.split("\n") if f"{EXCLUDED_PREFIX}ViewModel" not in line
    )


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Scaffold a feature module from feature/template.",
        epilog=(
            'Examples:\n'
            '  python3 scripts/create_feature.py userProfile\n'
            '  python3 scripts/create_feature.py userProfile --layers presentation,di\n'
            '  python3 scripts/create_feature.py userProfile --layers domain --force   # add a layer later\n'
            '  python3 scripts/create_feature.py userProfile --graph auth --dry-run\n'
            '\n'
            'Name it in any case — userProfile, user-profile and UserProfile all work.'
        ),
        formatter_class=argparse.RawDescriptionHelpFormatter,
    )
    parser.add_argument("name", help="Feature name, e.g. userProfile")
    parser.add_argument(
        "--layers",
        default=",".join(ALL_LAYERS),
        help=f"Comma-separated layers to generate. Default: all ({','.join(ALL_LAYERS)})",
    )
    parser.add_argument(
        "--graph",
        default="main",
        choices=[*NAV_GRAPHS, "none"],
        help="Entry block in AppNavHost.kt to register the destination in — the signed-in flow, one of\nits tabs, or the auth flow. Default: main",
    )
    parser.add_argument("--dry-run", action="store_true", help="Show what would happen, change nothing.")
    parser.add_argument("--force", action="store_true", help="Overwrite layers that already exist.")
    return parser.parse_args()


def resolve_layers(raw: str) -> list[str]:
    requested = [layer.strip() for layer in raw.split(",") if layer.strip()]
    unknown = [layer for layer in requested if layer not in ALL_LAYERS]
    if unknown:
        sys.exit(f"Unknown layer(s): {', '.join(unknown)}. Choose from: {', '.join(ALL_LAYERS)}")
    # Keep the canonical order regardless of how they were typed.
    return [layer for layer in ALL_LAYERS if layer in requested]


def strip_missing_layer_dependencies(text: str, flat: str, layers: list[str]) -> str:
    """
    Drops `api(projects.feature.<name>.<layer>)` lines for layers that were not generated, so that
    a partial feature (e.g. presentation + di only) still configures.
    """
    missing = [layer for layer in ALL_LAYERS if layer not in layers]
    if not missing:
        return text
    pattern = re.compile(
        rf"^\s*(?:api|implementation)\(projects\.feature\.{re.escape(flat)}\.(?:{'|'.join(missing)})\)\s*\n",
        re.MULTILINE,
    )
    return pattern.sub("", text)


def copy_layer(
    layer: str,
    flat: str,
    pascal: str,
    camel: str,
    snake: str,
    layers: list[str],
    dry_run: bool,
    force: bool,
) -> bool:
    source_dir = REPO_ROOT / "feature" / TEMPLATE_FEATURE / layer
    dest_dir = REPO_ROOT / "feature" / flat / layer

    if not source_dir.is_dir():
        print(f"  skipping {layer}: template {source_dir.relative_to(REPO_ROOT)} does not exist")
        return False

    if dest_dir.exists() and not force:
        print(f"  skipping {layer}: {dest_dir.relative_to(REPO_ROOT)} already exists (use --force)")
        return False

    for source_file in sorted(p for p in source_dir.rglob("*") if p.is_file()):
        relative = source_file.relative_to(source_dir)
        if not is_copyable(relative):
            continue
        dest_file = dest_dir / rewrite_relative_path(relative, flat, pascal)

        if source_file.suffix in TEXT_SUFFIXES:
            text = source_file.read_text()
            if source_file.name == "strings.xml":
                text = strip_args_strings(text)
            if source_file.name == "TemplateModule.kt":
                text = strip_args_registration(text)
            text = rewrite_source(text, flat, pascal, camel)
            # Before rewrite_resource_names: a tag's stem is camelCase, a resource's is snake_case.
            text = rewrite_test_tags(text, camel)
            # `template_title` -> `user_profile_title`, in the Kotlin references and in strings.xml.
            text = rewrite_resource_names(text, snake)
            if source_file.name == "build.gradle.kts":
                text = strip_missing_layer_dependencies(text, flat, layers)
            write_file(dest_file, text, dry_run)
        elif dry_run:
            print(f"  would copy {dest_file.relative_to(REPO_ROOT)}")
        else:
            dest_file.parent.mkdir(parents=True, exist_ok=True)
            shutil.copy2(source_file, dest_file)
            print(f"  copied {dest_file.relative_to(REPO_ROOT)}")

    return True


def register_in_settings(flat: str, layers: list[str], dry_run: bool) -> None:
    """
    Registers the generated layers, merging into the feature's existing block if there is one.

    The merge is what makes `--layers domain --force` work on a feature that already exists: without
    it the new layer would sit on disk unincluded, and only `doctor.py` would notice.
    """
    wanted = [LAYER_SUFFIX[layer] for layer in layers]
    ordered = [LAYER_SUFFIX[layer] for layer in ALL_LAYERS]

    block = "\n".join(
        [f'includeFeatureModule(', f'    "{flat}",']
        + [f"    ModuleSuffix.{suffix}," for suffix in wanted]
        + [")", "", ""]
    )
    # New features go above the template block, which stays last. Anchoring on the block rather
    # than on the comment above it keeps the anchor from drifting when that comment is reworded.
    anchor = TEMPLATE_ANCHOR
    existing_block = re.compile(
        rf'(includeFeatureModule\(\n    "{re.escape(flat)}",\n)((?:    ModuleSuffix\.\w+,\n)*)(\))'
    )

    def transform(text: str) -> str:
        match = existing_block.search(text)
        if match:
            present = re.findall(r"ModuleSuffix\.(\w+)", match.group(2))
            merged = [suffix for suffix in ordered if suffix in present or suffix in wanted]
            if merged == present:
                return text
            body = "".join(f"    ModuleSuffix.{suffix},\n" for suffix in merged)
            return text[: match.start()] + match.group(1) + body + match.group(3) + text[match.end():]
        index = text.find(anchor)
        if index != -1:
            # Insert before the comment introducing the template block, if there is one.
            start = text.rfind("\n\n", 0, index)
            start = index if start == -1 else start + 2
            return text[:start] + block + text[start:]
        return text.rstrip("\n") + "\n\n" + block.rstrip("\n") + "\n"

    edit_file(SETTINGS_FILE, transform, dry_run, "register modules")


def tree_description(layers: list[str]) -> str:
    """The note beside the module in CLAUDE.md's tree, so a reader can see the shape at a glance."""
    if layers == ALL_LAYERS:
        return "full stack"
    if layers == ["presentation", "di"]:
        return "screen only"
    return "partial stack"


def register_in_core_di_build(flat: str, dry_run: bool) -> None:
    """
    Adds `api(projects.feature.<name>.di)` to `:core:di`, without which `Koin.kt` cannot see the
    new feature's module.
    """
    entry = f"api(projects.feature.{flat}.di)"

    def transform(text: str) -> str:
        if entry in text:
            return text

        lines = text.split("\n")
        pattern = re.compile(r"^(\s*)api\(projects\.feature\.\w+\.di\)$")
        matches = [i for i, line in enumerate(lines) if pattern.match(line)]
        if matches:
            anchor = matches[-1]
            indent = pattern.match(lines[anchor]).group(1)
        else:
            anchor = next(i for i, line in enumerate(lines) if line.rstrip() == "dependencies {")
            indent = " " * 4
        lines.insert(anchor + 1, f"{indent}{entry}")
        return "\n".join(lines)

    edit_file(CORE_DI_BUILD_FILE, transform, dry_run, "add :core:di dependency")


def register_in_koin(flat: str, pascal: str, dry_run: bool) -> None:
    import_line = f"import {BASE_PACKAGE}.feature.{flat}.di.{pascal}Module"
    module_entry = f"{pascal}Module.module,"

    def transform(text: str) -> str:
        if module_entry in text:
            return text

        lines = text.split("\n")
        insert_import(lines, import_line)

        # Insert the module after the last `XModule.module,` entry of the appModules(...) list.
        # Scoped to that list rather than to the file: `debugMenuModules()` below it has the
        # same shape, and a feature registered there would exist only in dev and staging —
        # silently, because every check still passes and only a `prod` build is missing it.
        entry_pattern = re.compile(r"^(\s*)\w+Module\.module,$")
        start = next(i for i, line in enumerate(lines) if line.startswith(f"fun {KOIN_MODULE_LIST}("))
        end = next(i for i in range(start + 1, len(lines)) if lines[i].rstrip() == ")")
        last_entry = max(i for i in range(start, end) if entry_pattern.match(lines[i]))
        indent = entry_pattern.match(lines[last_entry]).group(1)
        lines.insert(last_entry + 1, f"{indent}{module_entry}")

        return "\n".join(lines)

    edit_file(KOIN_FILE, transform, dry_run, "register Koin module")


def main() -> None:
    args = parse_args()
    flat = to_flat(args.name)
    pascal = to_pascal(args.name)
    camel = to_camel(args.name)
    snake = to_snake(args.name)
    layers = resolve_layers(args.layers)

    if flat == TEMPLATE_FEATURE:
        sys.exit("Refusing to overwrite the template feature.")

    print(f"Creating feature '{flat}' (classes: {pascal}Xxx, strings: {snake}_xxx)")
    print(f"Layers: {', '.join(layers)}")
    if args.dry_run:
        print("-- dry run, nothing will be written --")

    created = [
        layer
        for layer in layers
        if copy_layer(layer, flat, pascal, camel, snake, layers, args.dry_run, args.force)
    ]
    if not created:
        sys.exit("Nothing was generated.")

    register_in_settings(flat, created, args.dry_run)
    # The tree documents what is on disk, which after `--layers ... --force` is more than was
    # just generated.
    present = [
        layer for layer in ALL_LAYERS
        if layer in created or (REPO_ROOT / "feature" / flat / layer).is_dir()
    ]
    register_in_feature_tree(flat, present, tree_description(present), args.dry_run)
    if "di" in created:
        register_in_core_di_build(flat, args.dry_run)
        register_in_koin(flat, pascal, args.dry_run)
    else:
        print("  no di layer generated — Koin registration skipped")

    if "presentation" in created:
        register_destination(
            # The screen's own sub-package (D34); a new feature's one screen is named after it,
            # so the directory is the feature's flat name.
            import_line=f"import {BASE_PACKAGE}.feature.{flat}.presentation.{flat}.{camel}Destination",
            call_line=f"{camel}Destination(backStack = backStack)",
            graph=args.graph,
            dry_run=args.dry_run,
        )

    print("\nDone. Run ./gradlew build")


if __name__ == "__main__":
    main()
