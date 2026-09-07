#!/usr/bin/env python3
"""
Copies the reusable `service/` modules into another project.

`service/` is written to be portable: nothing in it references `:core:*`, `:feature:*` or `:app`,
and it reads no `R` but its own. What it cannot do on its own is change its package — the sources
sit in `com.example.androidproject1.core.*` because that is this project's base package. This
script does the copy and that rewrite in one step, which is the part that is tedious and easy to
get half-right by hand.

    python3 scripts/export_service.py --to ~/Projects/android/MyNewApp --package com.acme.myapp

Afterwards, add the printed `includeServiceModule` block to the target's `settings.gradle.kts`
(along with the `ModuleSuffix` / `includeModule` helpers if it does not have them yet), and only
then create that project's own `core/` and `feature/`.
"""

from __future__ import annotations

import argparse
import re
import shutil
import sys
from pathlib import Path

from _common import BASE_PACKAGE, BASE_PATH, REPO_ROOT

SERVICE_DIR = REPO_ROOT / "service"

# Directories that are build output or IDE state rather than source.
SKIP_DIRS = {"build", ".gradle", ".kotlin", ".idea"}

# The modules that exist today, in the order settings.gradle.kts lists them.
SERVICE_MODULES = {
    "core": ["Domain", "Data", "Ui"],
}


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Copy service/ into another project, rewriting the base package.",
    )
    parser.add_argument(
        "--to",
        required=True,
        help="Root directory of the target project (it does not have to exist yet).",
    )
    parser.add_argument(
        "--package",
        default=BASE_PACKAGE,
        help=f"Base package for the copy. Default: unchanged ({BASE_PACKAGE}).",
    )
    parser.add_argument(
        "--modules",
        default=",".join(SERVICE_MODULES),
        help=f"Comma-separated service modules to copy. Default: all ({','.join(SERVICE_MODULES)}).",
    )
    parser.add_argument("--dry-run", action="store_true", help="Print what would happen.")
    parser.add_argument(
        "--force",
        action="store_true",
        help="Overwrite modules that already exist in the target.",
    )
    return parser.parse_args()


def validate_package(package: str) -> None:
    if not re.fullmatch(r"[a-z][a-z0-9_]*(\.[a-z][a-z0-9_]*)+", package):
        sys.exit(
            f"'{package}' is not a valid lowercase Java package "
            "(expected something like com.acme.myapp)."
        )


def rewrite_text(text: str, package: str) -> str:
    """
    Rewrites the base package everywhere it appears.

    Unlike `create_feature.py` this *is* a blanket replacement, and safely so: here the whole point
    is to move `com.example.androidproject1` wholesale, and the word `example` never appears in
    `service/` except as part of that package.
    """
    return text.replace(BASE_PACKAGE, package)


def rewrite_relative_path(relative: Path, package: str) -> Path:
    """Moves `.../com/example/androidproject1/...` to the target package's directory layout."""
    return Path(relative.as_posix().replace(BASE_PATH, package.replace(".", "/")))


def iter_source_files(module_dir: Path):
    for path in sorted(module_dir.rglob("*")):
        if not path.is_file():
            continue
        if SKIP_DIRS.intersection(path.relative_to(module_dir).parts):
            continue
        yield path


def copy_module(
    name: str,
    target_root: Path,
    package: str,
    dry_run: bool,
    force: bool,
) -> int:
    source_dir = SERVICE_DIR / name
    if not source_dir.is_dir():
        sys.exit(f"No such service module: {source_dir}")

    destination_dir = target_root / "service" / name
    if destination_dir.exists() and not force:
        sys.exit(f"{destination_dir} already exists. Re-run with --force to overwrite.")

    count = 0
    for path in iter_source_files(source_dir):
        relative = rewrite_relative_path(path.relative_to(source_dir), package)
        destination = destination_dir / relative

        if path.suffix in {".kt", ".kts", ".xml", ".pro", ".md"}:
            content = rewrite_text(path.read_text(), package)
            if dry_run:
                print(f"  would write {destination}")
            else:
                destination.parent.mkdir(parents=True, exist_ok=True)
                destination.write_text(content)
                print(f"  wrote {destination}")
        elif dry_run:
            print(f"  would copy {destination}")
        else:
            destination.parent.mkdir(parents=True, exist_ok=True)
            shutil.copy2(path, destination)
            print(f"  copied {destination}")
        count += 1

    return count


def settings_snippet(modules: list[str]) -> str:
    lines = []
    for name in modules:
        suffixes = "\n".join(f"    ModuleSuffix.{s}," for s in SERVICE_MODULES[name])
        lines.append(f'includeServiceModule(\n    "{name}",\n{suffixes}\n)')
    return "\n\n".join(lines)


def main() -> None:
    args = parse_args()
    validate_package(args.package)

    modules = [m.strip() for m in args.modules.split(",") if m.strip()]
    unknown = [m for m in modules if m not in SERVICE_MODULES]
    if unknown:
        sys.exit(
            f"Unknown service module(s): {', '.join(unknown)}. "
            f"Choose from: {', '.join(SERVICE_MODULES)}"
        )

    target_root = Path(args.to).expanduser().resolve()
    if target_root == REPO_ROOT:
        sys.exit("Target is this project. Pass --to with a different directory.")

    print(f"Exporting service modules to {target_root}")
    print(f"Modules: {', '.join(modules)}")
    print(f"Package: {BASE_PACKAGE} -> {args.package}")
    if args.dry_run:
        print("-- dry run, nothing will be written --")

    total = sum(copy_module(m, target_root, args.package, args.dry_run, args.force) for m in modules)

    print(f"\n{total} files.")
    print("\nAdd to the target's settings.gradle.kts:\n")
    print(settings_snippet(modules))
    print(
        "\nThen create that project's own core/ (theme + Koin) and feature/ modules, "
        "and check gradle/libs.versions.toml has: kotlinx-coroutines-core, "
        "androidx-datastore-preferences, the Compose BOM bundle, androidx-activity-compose, "
        "androidx-lifecycle-viewmodel-ktx, junit, kotlinx-coroutines-test."
    )


if __name__ == "__main__":
    main()
