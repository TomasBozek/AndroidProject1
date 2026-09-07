#!/usr/bin/env python3
"""
Scaffolds a new feature module from `feature/example`.

    python3 scripts/create_feature.py chatRoom
    python3 scripts/create_feature.py chatRoom --layers presentation,di
    python3 scripts/create_feature.py chatRoom --dry-run

Unlike a plain copy, this also registers the new modules in `settings.gradle.kts` and the feature's
Koin module in `core/di/.../Koin.kt`, which are the two steps that are easy to forget.
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
    BASE_PATH,
    CORE_DI_BUILD_FILE,
    KOIN_FILE,
    LAYER_SUFFIX,
    REPO_ROOT,
    SETTINGS_FILE,
    TEMPLATE_FEATURE,
    edit_file,
    rewrite_relative_path,
    rewrite_source,
    to_flat,
    to_pascal,
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
    parser = argparse.ArgumentParser(description="Scaffold a feature module from feature/example.")
    parser.add_argument("name", help="Feature name, e.g. chatRoom")
    parser.add_argument(
        "--layers",
        default=",".join(ALL_LAYERS),
        help=f"Comma-separated layers to generate. Default: all ({','.join(ALL_LAYERS)})",
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


def copy_layer(layer: str, flat: str, pascal: str, layers: list[str], dry_run: bool, force: bool) -> bool:
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
    block = "\n".join(
        [f'includeFeatureModule(', f'    "{flat}",']
        + [f"    ModuleSuffix.{LAYER_SUFFIX[layer]}," for layer in layers]
        + [")", "", ""]
    )
    marker = "// Template module cloned by"

    def transform(text: str) -> str:
        if f'includeFeatureModule(\n    "{flat}",' in text:
            return text
        if marker in text:
            return text.replace(marker, block + marker, 1)
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
    import_line = f"import com.example.androidproject1.feature.{flat}.di.{pascal}Module"
    module_entry = f"{pascal}Module.module,"

    def transform(text: str) -> str:
        if module_entry in text:
            return text

        lines = text.split("\n")

        # Insert the import into the existing sorted import block.
        import_indexes = [i for i, line in enumerate(lines) if line.startswith("import ")]
        insert_at = next(
            (i for i in import_indexes if lines[i] > import_line),
            import_indexes[-1] + 1 if import_indexes else 0,
        )
        lines.insert(insert_at, import_line)

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
    layers = resolve_layers(args.layers)

    if flat == TEMPLATE_FEATURE:
        sys.exit("Refusing to overwrite the template feature.")

    print(f"Creating feature '{flat}' (classes: {pascal}Xxx)")
    print(f"Layers: {', '.join(layers)}")
    if args.dry_run:
        print("-- dry run, nothing will be written --")

    created = [layer for layer in layers if copy_layer(layer, flat, pascal, layers, args.dry_run, args.force)]
    if not created:
        sys.exit("Nothing was generated.")

    register_in_settings(flat, created, args.dry_run)
    if "di" in created:
        register_in_core_di_build(flat, args.dry_run)
        register_in_koin(flat, pascal, args.dry_run)
    else:
        print("  no di layer generated — Koin registration skipped")

    print(f"\nDone. Next: add the destination to app/.../AppNavHost.kt, then run ./gradlew build")


if __name__ == "__main__":
    main()
