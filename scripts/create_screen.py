#!/usr/bin/env python3
"""
Scaffolds a screen (the six-file Destination/Screen/State/Event/Direction/ViewModel unit)
into an existing feature, cloned from `feature/example`.

    python3 scripts/create_screen.py chatroom ChatRoomList
    python3 scripts/create_screen.py chatroom ChatRoomDetail --sub detail
    python3 scripts/create_screen.py chatroom ChatRoomList --dry-run

Also registers the new ViewModel in the feature's Koin module, and prints the line to add to
`AppNavHost.kt`.
"""

from __future__ import annotations

import re
import sys
import argparse
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))

from _common import (  # noqa: E402
    BASE_PACKAGE,
    BASE_PATH,
    REPO_ROOT,
    TEMPLATE_CLASS,
    TEMPLATE_FEATURE,
    edit_file,
    to_camel,
    to_flat,
    to_pascal,
    write_file,
)

TEMPLATE_DIR = (
    REPO_ROOT / "feature" / TEMPLATE_FEATURE / "presentation/src/main/kotlin"
    / BASE_PATH / "feature" / TEMPLATE_FEATURE / "presentation"
)


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Scaffold a screen inside an existing feature.")
    parser.add_argument("feature", help="Existing feature module name, e.g. chatroom")
    parser.add_argument("screen", help="Screen name in PascalCase, e.g. ChatRoomList")
    parser.add_argument("--sub", default="", help="Optional sub-package, e.g. overview or overview/list")
    parser.add_argument("--dry-run", action="store_true", help="Show what would happen, change nothing.")
    parser.add_argument("--force", action="store_true", help="Overwrite files that already exist.")
    return parser.parse_args()


def rewrite(text: str, feature: str, screen_pascal: str, screen_camel: str, sub_package: str) -> str:
    # The feature's package segment.
    text = text.replace(f"feature.{TEMPLATE_FEATURE}", f"feature.{feature}")
    # camelCase identifiers derived from the screen, e.g. `exampleDestination`.
    text = re.sub(rf"\b{TEMPLATE_FEATURE}(?=[A-Z])", screen_camel, text)
    # Class names.
    text = text.replace(TEMPLATE_CLASS, screen_pascal)

    if sub_package:
        text = text.replace(
            f"package {BASE_PACKAGE}.feature.{feature}.presentation",
            f"package {BASE_PACKAGE}.feature.{feature}.presentation.{sub_package}",
            1,
        )
    return text


def register_in_koin(feature: str, screen_pascal: str, sub_package: str, dry_run: bool) -> None:
    di_dir = REPO_ROOT / "feature" / feature / "di/src/main/kotlin" / BASE_PATH / "feature" / feature / "di"
    modules = sorted(di_dir.glob("*Module.kt")) if di_dir.is_dir() else []
    if not modules:
        print(f"  no Koin module found under feature/{feature}/di — register the ViewModel manually")
        return

    module_file = modules[0]
    package_suffix = f".{sub_package}" if sub_package else ""
    import_line = f"import {BASE_PACKAGE}.feature.{feature}.presentation{package_suffix}.{screen_pascal}ViewModel"
    entry = f"viewModelOf(::{screen_pascal}ViewModel)"

    def transform(text: str) -> str:
        if entry in text:
            return text

        lines = text.split("\n")

        import_indexes = [i for i, line in enumerate(lines) if line.startswith("import ")]
        insert_at = next(
            (i for i in import_indexes if lines[i] > import_line),
            import_indexes[-1] + 1 if import_indexes else 0,
        )
        lines.insert(insert_at, import_line)

        # Insert after the last existing viewModelOf(...) line, else right after `module {`.
        view_model_pattern = re.compile(r"^(\s*)viewModelOf\(.*\)$")
        matches = [i for i, line in enumerate(lines) if view_model_pattern.match(line)]
        if matches:
            anchor = matches[-1]
            indent = view_model_pattern.match(lines[anchor]).group(1)
        else:
            anchor = next(i for i, line in enumerate(lines) if line.rstrip().endswith("= module {"))
            indent = " " * 8
        lines.insert(anchor + 1, f"{indent}{entry}")

        return "\n".join(lines)

    edit_file(module_file, transform, dry_run, "register ViewModel")


def main() -> None:
    args = parse_args()

    feature = to_flat(args.feature)
    screen_pascal = to_pascal(args.screen)
    screen_camel = to_camel(args.screen)
    sub_path = args.sub.strip("/")
    sub_package = sub_path.replace("/", ".")

    feature_presentation = REPO_ROOT / "feature" / feature / "presentation"
    if not feature_presentation.is_dir():
        sys.exit(
            f"No presentation module at feature/{feature}/presentation. "
            f"Create the feature first: python3 scripts/create_feature.py {feature}"
        )

    dest_dir = feature_presentation / "src/main/kotlin" / BASE_PATH / "feature" / feature / "presentation"
    if sub_path:
        dest_dir = dest_dir / sub_path

    sources = sorted(TEMPLATE_DIR.glob(f"{TEMPLATE_CLASS}*.kt"))
    if not sources:
        sys.exit(f"No template files found in {TEMPLATE_DIR}")

    print(f"Creating screen '{screen_pascal}' in feature '{feature}'")
    print(f"Target: {dest_dir.relative_to(REPO_ROOT)}")
    if args.dry_run:
        print("-- dry run, nothing will be written --")

    wrote_any = False
    for source in sources:
        dest = dest_dir / source.name.replace(TEMPLATE_CLASS, screen_pascal)
        if dest.exists() and not args.force:
            print(f"  skipping {dest.relative_to(REPO_ROOT)}: already exists (use --force)")
            continue
        write_file(dest, rewrite(source.read_text(), feature, screen_pascal, screen_camel, sub_package), args.dry_run)
        wrote_any = True

    if not wrote_any:
        sys.exit("Nothing was generated.")

    register_in_koin(feature, screen_pascal, sub_package, args.dry_run)

    package_suffix = f".{sub_package}" if sub_package else ""
    print("\nAdd to app/src/main/java/com/example/androidproject1/AppNavHost.kt:")
    print(f"    import {BASE_PACKAGE}.feature.{feature}.presentation{package_suffix}.{screen_camel}Destination")
    print(f"    {screen_camel}Destination(navController = navController)")


if __name__ == "__main__":
    main()
