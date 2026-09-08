#!/usr/bin/env python3
"""
Scaffolds a new feature module from `feature/template`.

    python3 scripts/create_feature.py userProfile
    python3 scripts/create_feature.py userProfile --layers presentation,di
    python3 scripts/create_feature.py userProfile --graph auth
    python3 scripts/create_feature.py userProfile --dry-run

Unlike a plain copy, this also performs every registration step the new module needs:
`settings.gradle.kts`, `:core:di`'s build file, the Koin module list and `AppNavHost.kt`.
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
    rewrite_relative_path,
    rewrite_resource_names,
    rewrite_source,
    to_flat,
    to_pascal,
    to_snake,
    write_file,
)

TEXT_SUFFIXES = {".kt", ".kts", ".xml", ".pro"}

# Generated or IDE-local directories that must never be cloned into a new module.
EXCLUDED_DIRS = {"build", ".gradle", ".idea", ".cxx"}
EXCLUDED_FILES = {".DS_Store"}


def is_copyable(relative: Path) -> bool:
    if EXCLUDED_DIRS.intersection(relative.parts):
        return False
    return relative.name not in EXCLUDED_FILES


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Scaffold a feature module from feature/template.")
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
        help="Nav graph in AppNavHost.kt to register the destination in. Default: main",
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
            text = rewrite_source(source_file.read_text(), flat, pascal)
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

        # Insert the module after the last `XModule.module,` entry in the modules(...) call.
        entry_pattern = re.compile(r"^(\s*)\w+Module\.module,$")
        last_entry = max(i for i, line in enumerate(lines) if entry_pattern.match(line))
        indent = entry_pattern.match(lines[last_entry]).group(1)
        lines.insert(last_entry + 1, f"{indent}{module_entry}")

        return "\n".join(lines)

    edit_file(KOIN_FILE, transform, dry_run, "register Koin module")


def main() -> None:
    args = parse_args()
    flat = to_flat(args.name)
    pascal = to_pascal(args.name)
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
        if copy_layer(layer, flat, pascal, snake, layers, args.dry_run, args.force)
    ]
    if not created:
        sys.exit("Nothing was generated.")

    register_in_settings(flat, created, args.dry_run)
    if "di" in created:
        register_in_core_di_build(flat, args.dry_run)
        register_in_koin(flat, pascal, args.dry_run)
    else:
        print("  no di layer generated — Koin registration skipped")

    if "presentation" in created:
        register_destination(
            import_line=f"import {BASE_PACKAGE}.feature.{flat}.presentation.{flat}Destination",
            call_line=f"{flat}Destination(navController = navController)",
            graph=args.graph,
            dry_run=args.dry_run,
        )

    print("\nDone. Run ./gradlew build")


if __name__ == "__main__":
    main()
