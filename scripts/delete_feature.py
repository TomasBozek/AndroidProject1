#!/usr/bin/env python3
"""
Removes a feature module and every registration `create_feature.py` made for it.

    python3 scripts/delete_feature.py userProfile
    python3 scripts/delete_feature.py userProfile --dry-run

This is the inverse of `create_feature.py`: it deletes `feature/<name>/`, the
`includeFeatureModule` block in `settings.gradle.kts`, the `:core:di` dependency, the Koin module
entry and the destinations registered in `AppNavHost.kt` — the same four places that are easy to
leave half-edited by hand, which is what turns a throwaway experiment into a broken build.

Cross-feature references (a `navigateToX = { ... }` lambda in another destination, say) cannot be
removed safely and are reported instead.
"""

from __future__ import annotations

import argparse
import re
import shutil
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))

from _common import (  # noqa: E402
    APP_NAV_HOST_FILE,
    BASE_PACKAGE,
    CORE_DI_BUILD_FILE,
    KOIN_FILE,
    REPO_ROOT,
    TEMPLATE_FEATURE,
    edit_file,
    feature_koin_module_file,
    relative_to_repo,
    remove_lines,
    to_flat,
)

# Directories scanned for leftover references once the module is gone.
REFERENCE_ROOTS = ["app", "core", "feature", "service", "settings.gradle.kts"]


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Delete a feature module and its registrations.")
    parser.add_argument("name", help="Feature name, e.g. userProfile")
    parser.add_argument("--dry-run", action="store_true", help="Show what would happen, change nothing.")
    parser.add_argument(
        "--force",
        action="store_true",
        help="Allow deleting the template feature, which the generators clone from.",
    )
    return parser.parse_args()


def koin_module_class(flat: str) -> str | None:
    """The feature's Koin object name, read off disk rather than guessed from the feature name."""
    module_file = feature_koin_module_file(flat)
    return module_file.stem if module_file else None


def destination_functions(flat: str) -> list[str]:
    """
    The `xDestination` functions AppNavHost imports from this feature.

    Read from AppNavHost's own imports rather than from the feature's sources, so a screen in a
    sub-package or one named differently from the feature is still found.
    """
    if not APP_NAV_HOST_FILE.is_file():
        return []
    prefix = f"import {BASE_PACKAGE}.feature.{flat}.presentation"
    names = []
    for line in APP_NAV_HOST_FILE.read_text().split("\n"):
        if line.startswith(prefix):
            name = line.rsplit(".", 1)[-1].strip()
            if name.endswith("Destination") and name[0].islower():
                names.append(name)
    return names


def delete_directory(flat: str, dry_run: bool) -> bool:
    directory = REPO_ROOT / "feature" / flat
    if not directory.is_dir():
        print(f"  feature/{flat} does not exist")
        return False
    if dry_run:
        print(f"  would delete {relative_to_repo(directory)}/")
        return True
    shutil.rmtree(directory)
    print(f"  deleted {relative_to_repo(directory)}/")
    return True


def unregister_from_settings(flat: str, dry_run: bool) -> None:
    block = re.compile(
        rf'includeFeatureModule\(\n    "{re.escape(flat)}",\n(?:    ModuleSuffix\.\w+,\n)*\)\n\n?',
    )
    edit_file(
        REPO_ROOT / "settings.gradle.kts",
        lambda text: block.sub("", text),
        dry_run,
        "unregister modules",
    )


def unregister_from_core_di_build(flat: str, dry_run: bool) -> None:
    entry = f"api(projects.feature.{flat}.di)"
    edit_file(
        CORE_DI_BUILD_FILE,
        lambda text: remove_lines(text, lambda line: line.strip() == entry),
        dry_run,
        "remove :core:di dependency",
    )


def unregister_from_koin(flat: str, module_class: str | None, dry_run: bool) -> None:
    if module_class is None:
        print("  Koin: feature has no di module, nothing to unregister")
        return

    import_line = f"import {BASE_PACKAGE}.feature.{flat}.di.{module_class}"
    entry = f"{module_class}.module,"

    def transform(text: str) -> str:
        return remove_lines(text, lambda line: line == import_line or line.strip() == entry)

    edit_file(KOIN_FILE, transform, dry_run, "unregister Koin module")


def unregister_destinations(flat: str, functions: list[str], dry_run: bool) -> None:
    if not functions:
        print("  AppNavHost: no destinations from this feature")
        return

    import_prefix = f"import {BASE_PACKAGE}.feature.{flat}."
    calls = tuple(f"{name}(" for name in functions)

    def transform(text: str) -> str:
        return remove_lines(
            text,
            lambda line: line.startswith(import_prefix) or line.strip().startswith(calls),
        )

    edit_file(APP_NAV_HOST_FILE, transform, dry_run, f"remove {', '.join(functions)}")


def report_leftovers(flat: str) -> None:
    """Reports references the script cannot remove — typically cross-feature navigation lambdas."""
    needles = (f"feature.{flat}.", f"feature/{flat}", f'"{flat}"')
    hits = []
    for root in REFERENCE_ROOTS:
        path = REPO_ROOT / root
        files = [path] if path.is_file() else [
            p for p in path.rglob("*")
            if p.is_file() and p.suffix in {".kt", ".kts", ".xml"} and "build" not in p.relative_to(path).parts
        ] if path.exists() else []
        for file in files:
            for number, line in enumerate(file.read_text(errors="ignore").split("\n"), start=1):
                if any(needle in line for needle in needles):
                    hits.append(f"    {relative_to_repo(file)}:{number}: {line.strip()}")

    if hits:
        print(f"\n{len(hits)} leftover reference(s) to '{flat}' — remove these by hand:")
        print("\n".join(hits))


def main() -> None:
    args = parse_args()
    flat = to_flat(args.name)

    if flat == TEMPLATE_FEATURE and not args.force:
        sys.exit(
            "Refusing to delete the template feature the generators clone from. Use --force if "
            "you really mean it."
        )

    if not (REPO_ROOT / "feature" / flat).is_dir():
        sys.exit(f"No such feature: feature/{flat}")

    print(f"Deleting feature '{flat}'")
    if args.dry_run:
        print("-- dry run, nothing will be written --")

    # Both are read before the directory goes away.
    module_class = koin_module_class(flat)
    functions = destination_functions(flat)

    unregister_destinations(flat, functions, args.dry_run)
    unregister_from_koin(flat, module_class, args.dry_run)
    unregister_from_core_di_build(flat, args.dry_run)
    unregister_from_settings(flat, args.dry_run)
    delete_directory(flat, args.dry_run)

    if not args.dry_run:
        report_leftovers(flat)

    print("\nDone. Run ./gradlew build")


if __name__ == "__main__":
    main()
