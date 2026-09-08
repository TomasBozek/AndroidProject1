#!/usr/bin/env python3
"""
Scaffolds a screen (the six-file Destination/Screen/State/Event/Navigation/ViewModel unit)
into an existing feature, cloned from `feature/template`.

    python3 scripts/create_screen.py userprofile UserProfileList
    python3 scripts/create_screen.py userprofile UserProfileDetail --sub detail
    python3 scripts/create_screen.py userprofile UserProfileList --graph none
    python3 scripts/create_screen.py userprofile UserProfileList --dry-run

Also renames the template's string resources into the new screen's namespace and merges them into
the feature's `strings.xml`, registers the ViewModel in the feature's Koin module, and registers
the destination in `AppNavHost.kt`.
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
    NAV_GRAPHS,
    REPO_ROOT,
    TEMPLATE_CLASS,
    TEMPLATE_FEATURE,
    TEMPLATE_RESOURCE_PREFIX,
    edit_file,
    feature_koin_module_file,
    insert_after_last,
    insert_import,
    merge_strings_xml,
    module_namespace,
    read_string_resources,
    register_destination,
    to_camel,
    to_flat,
    to_pascal,
    to_snake,
    write_file,
)

TEMPLATE_PRESENTATION_DIR = REPO_ROOT / "feature" / TEMPLATE_FEATURE / "presentation"

TEMPLATE_DIR = (
    TEMPLATE_PRESENTATION_DIR / "src/main/kotlin"
    / BASE_PATH / "feature" / TEMPLATE_FEATURE / "presentation"
)

TEMPLATE_TEST_DIR = (
    TEMPLATE_PRESENTATION_DIR / "src/test/kotlin"
    / BASE_PATH / "feature" / TEMPLATE_FEATURE / "presentation"
)

TEMPLATE_STRINGS = TEMPLATE_PRESENTATION_DIR / "src/main/res/values/strings.xml"


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Scaffold a screen inside an existing feature.")
    parser.add_argument("feature", help="Existing feature module name, e.g. userprofile")
    parser.add_argument("screen", help="Screen name in PascalCase, e.g. UserProfileList")
    parser.add_argument("--sub", default="", help="Optional sub-package, e.g. overview or overview/list")
    parser.add_argument(
        "--graph",
        default="main",
        choices=[*NAV_GRAPHS, "none"],
        help="Nav graph in AppNavHost.kt to register the destination in. Default: main",
    )
    parser.add_argument("--dry-run", action="store_true", help="Show what would happen, change nothing.")
    parser.add_argument("--force", action="store_true", help="Overwrite files that already exist.")
    return parser.parse_args()


def template_string_entries(screen_pascal: str, screen_snake: str) -> dict[str, str]:
    """
    Maps the template's `template_*` strings onto the new screen's `<snake>_*` names.

    Without this a generated screen references `R.string.template_title`, which only exists in the
    template feature — and two screens in one feature would fight over the same name.
    """
    entries = {}
    for name, value in read_string_resources(TEMPLATE_STRINGS).items():
        if not name.startswith(f"{TEMPLATE_RESOURCE_PREFIX}_"):
            continue
        suffix = name[len(TEMPLATE_RESOURCE_PREFIX) + 1:]
        entries[f"{screen_snake}_{suffix}"] = value.replace(TEMPLATE_CLASS, screen_pascal)
    return entries


def rewrite(
    text: str,
    feature: str,
    screen_pascal: str,
    screen_camel: str,
    screen_snake: str,
    sub_package: str,
    r_import: str,
) -> str:
    # The feature's package segment.
    text = text.replace(f"feature.{TEMPLATE_FEATURE}", f"feature.{feature}")
    # camelCase identifiers derived from the screen, e.g. `templateDestination`.
    text = re.sub(rf"\b{TEMPLATE_FEATURE}(?=[A-Z])", screen_camel, text)
    # Class names.
    text = text.replace(TEMPLATE_CLASS, screen_pascal)
    # `R.string.template_title` -> `R.string.user_profile_list_title`.
    text = re.sub(rf"\b{TEMPLATE_RESOURCE_PREFIX}_(\w+)", rf"{screen_snake}_\1", text)

    if sub_package:
        text = text.replace(
            f"package {BASE_PACKAGE}.feature.{feature}.presentation",
            f"package {BASE_PACKAGE}.feature.{feature}.presentation.{sub_package}",
            1,
        )
        # R lives in the module's namespace package, which the sub-package is no longer part of.
        if re.search(r"\bR\.\w+\.", text):
            lines = text.split("\n")
            insert_import(lines, r_import)
            text = "\n".join(lines)

    return text


def register_in_koin(feature: str, screen_pascal: str, sub_package: str, dry_run: bool) -> None:
    module_file = feature_koin_module_file(feature)
    if module_file is None:
        print(f"  no Koin module found under feature/{feature}/di — register the ViewModel manually")
        return

    package_suffix = f".{sub_package}" if sub_package else ""
    import_line = f"import {BASE_PACKAGE}.feature.{feature}.presentation{package_suffix}.{screen_pascal}ViewModel"
    entry = f"viewModelOf(::{screen_pascal}ViewModel)"

    def transform(text: str) -> str:
        if entry in text:
            return text

        lines = text.split("\n")
        insert_import(lines, import_line)
        insert_after_last(
            lines,
            re.compile(r"^(\s*)viewModelOf\(.*\)$"),
            entry,
            re.compile(r"^.*= module \{$"),
            " " * 8,
        )
        return "\n".join(lines)

    edit_file(module_file, transform, dry_run, "register ViewModel")


def main() -> None:
    args = parse_args()

    feature = to_flat(args.feature)
    screen_pascal = to_pascal(args.screen)
    screen_camel = to_camel(args.screen)
    screen_snake = to_snake(args.screen)
    sub_path = args.sub.strip("/")
    sub_package = sub_path.replace("/", ".")

    feature_presentation = REPO_ROOT / "feature" / feature / "presentation"
    if not feature_presentation.is_dir():
        sys.exit(
            f"No presentation module at feature/{feature}/presentation. "
            f"Create the feature first: python3 scripts/create_feature.py {feature}"
        )

    presentation_package = f"{BASE_PACKAGE}.feature.{feature}.presentation"
    namespace = module_namespace(feature_presentation, presentation_package)
    r_import = f"import {namespace}.R"

    def screen_dir(source_set: str) -> Path:
        directory = (
            feature_presentation / "src" / source_set / "kotlin"
            / BASE_PATH / "feature" / feature / "presentation"
        )
        return directory / sub_path if sub_path else directory

    dest_dir = screen_dir("main")

    # (template file, where it goes). The seventh file is the ViewModel test, which lives in the
    # test source set — so a generated screen starts testable rather than becoming testable later.
    sources = [(path, dest_dir) for path in sorted(TEMPLATE_DIR.glob(f"{TEMPLATE_CLASS}*.kt"))]
    sources += [(path, screen_dir("test")) for path in sorted(TEMPLATE_TEST_DIR.glob(f"{TEMPLATE_CLASS}*.kt"))]
    if not sources:
        sys.exit(f"No template files found in {TEMPLATE_DIR}")

    print(f"Creating screen '{screen_pascal}' in feature '{feature}' (strings: {screen_snake}_xxx)")
    print(f"Target: {dest_dir.relative_to(REPO_ROOT)}")
    if args.dry_run:
        print("-- dry run, nothing will be written --")

    wrote_any = False
    for source, target_dir in sources:
        dest = target_dir / source.name.replace(TEMPLATE_CLASS, screen_pascal)
        if dest.exists() and not args.force:
            print(f"  skipping {dest.relative_to(REPO_ROOT)}: already exists (use --force)")
            continue
        text = rewrite(
            source.read_text(),
            feature,
            screen_pascal,
            screen_camel,
            screen_snake,
            sub_package,
            r_import,
        )
        write_file(dest, text, args.dry_run)
        wrote_any = True

    if not wrote_any:
        sys.exit("Nothing was generated.")

    merge_strings_xml(
        feature_presentation / "src/main/res/values/strings.xml",
        template_string_entries(screen_pascal, screen_snake),
        args.dry_run,
    )

    register_in_koin(feature, screen_pascal, sub_package, args.dry_run)

    package_suffix = f".{sub_package}" if sub_package else ""
    register_destination(
        import_line=f"import {BASE_PACKAGE}.feature.{feature}.presentation{package_suffix}.{screen_camel}Destination",
        call_line=f"{screen_camel}Destination(navController = navController)",
        graph=args.graph,
        dry_run=args.dry_run,
    )

    print("\nDone. Run ./gradlew build")


if __name__ == "__main__":
    main()
