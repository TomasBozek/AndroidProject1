#!/usr/bin/env python3
"""
Copies the reusable `service/` modules into another project.

`service/` is written to be portable: nothing in it references `:core:*`, `:feature:*` or `:app`,
and it reads no `R` but its own. What it cannot do on its own is change its package — the sources
sit in `com.example.androidproject1.core.*` because that is this project's base package. This
script does the copy and the package rewrite; the version catalog entries the copied build files
need are merged by hand from this project's own `gradle/libs.versions.toml`, which the script's own
output points at.

`build-logic/` comes along with it: the service build files apply the `convention.*` plugins, so
the modules do not compile without it.

    python3 scripts/export_service.py --to ~/Projects/android/MyNewApp --package com.acme.myapp

Afterwards, add the printed `includeBuild` and `includeServiceModule` block to the target's
`settings.gradle.kts` (along with the `ModuleSuffix` / `includeModule` helpers if it does not have
them yet) and `basePackage` to its `gradle.properties`, and only then create that project's own
`core/` and `feature/`.
"""

from __future__ import annotations

import argparse
import re
import shutil
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))

from _common import (  # noqa: E402
    BASE_PACKAGE,
    BASE_PATH,
    LAYER_SUFFIX,
    REPO_ROOT,
)

SERVICE_DIR = REPO_ROOT / "service"
BUILD_LOGIC_DIR = REPO_ROOT / "build-logic"

# Directories that are build output or IDE state rather than source.
SKIP_DIRS = {"build", ".gradle", ".kotlin", ".idea"}

TEXT_SUFFIXES = {".kt", ".kts", ".xml", ".pro", ".md", ".conf"}

# The order layers are listed in settings.gradle.kts.
SERVICE_LAYER_ORDER = ["domain", "data", "ui", "presentation", "di"]

LAYER_SUFFIXES = {**LAYER_SUFFIX, "ui": "Ui"}


# --------------------------------------------------------------------------------------------
# Module discovery
# --------------------------------------------------------------------------------------------


def discover_service_modules() -> dict[str, list[str]]:
    """
    Reads `service/` off disk rather than hardcoding it, so adding `service/network` needs no edit
    to this script.
    """
    modules: dict[str, list[str]] = {}
    if not SERVICE_DIR.is_dir():
        return modules
    for module in sorted(p for p in SERVICE_DIR.iterdir() if p.is_dir()):
        layers = [
            layer.name
            for layer in sorted(module.iterdir())
            if layer.is_dir() and (layer / "build.gradle.kts").is_file() and layer.name in LAYER_SUFFIXES
        ]
        if layers:
            modules[module.name] = sorted(layers, key=SERVICE_LAYER_ORDER.index)
        elif (module / "build.gradle.kts").is_file():
            # A flat service: one module, no domain/data/ui split. `service/network` is one, and
            # an empty layer list is what tells settings_snippet to print includeModule instead.
            modules[module.name] = []
    return modules


SERVICE_MODULES = discover_service_modules()


# --------------------------------------------------------------------------------------------
# Arguments
# --------------------------------------------------------------------------------------------


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Copy service/ into another project, rewriting the base package.",
        epilog=(
            'Examples:\n'
            '  python3 scripts/export_service.py --to ~/Projects/OtherApp --package com.acme.other\n'
            '\n'
            'Copies service/ into another project. To rename *this* project instead, use init_project.py.'
        ),
        formatter_class=argparse.RawDescriptionHelpFormatter,
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


# --------------------------------------------------------------------------------------------
# Copying
# --------------------------------------------------------------------------------------------


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

        if path.suffix in TEXT_SUFFIXES:
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


# --------------------------------------------------------------------------------------------
# build-logic/
# --------------------------------------------------------------------------------------------


def copy_build_logic(target_root: Path, package: str, dry_run: bool, force: bool) -> int:
    """
    Copies the included build the service modules' `convention.*` plugin ids resolve to.

    No package directories to move: the plugin classes live in the default package, and only the
    `group` and a fallback or two name the base package at all.
    """
    destination_dir = target_root / "build-logic"
    if destination_dir.exists() and not force:
        sys.exit(f"{destination_dir} already exists. Re-run with --force to overwrite.")

    count = 0
    for path in iter_source_files(BUILD_LOGIC_DIR):
        destination = destination_dir / path.relative_to(BUILD_LOGIC_DIR)
        if dry_run:
            print(f"  would write {destination}")
        else:
            destination.parent.mkdir(parents=True, exist_ok=True)
            if path.suffix in TEXT_SUFFIXES:
                destination.write_text(rewrite_text(path.read_text(), package))
            else:
                shutil.copy2(path, destination)
            print(f"  wrote {destination}")
        count += 1
    return count


def settings_snippet(modules: list[str]) -> str:
    lines = []
    for name in modules:
        if not SERVICE_MODULES[name]:
            lines.append(f'includeModule(":service:{name}", "service/{name}")')
            continue
        suffixes = "\n".join(f"    ModuleSuffix.{LAYER_SUFFIXES[layer]}," for layer in SERVICE_MODULES[name])
        lines.append(f'includeServiceModule(\n    "{name}",\n{suffixes}\n)')
    return "\n\n".join(lines)


def main() -> None:
    args = parse_args()
    validate_package(args.package)

    if not SERVICE_MODULES:
        sys.exit(f"No service modules found under {SERVICE_DIR}")

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
    # Built before the f-string rather than inside it: nesting the same quote character within an
    # f-string expression is PEP 701 syntax, which only parses on 3.12, and README.md promises 3.10+.
    described = ", ".join(f"{m} ({', '.join(SERVICE_MODULES[m]) or 'flat'})" for m in modules)
    print(f"Modules: {described}")
    print(f"Package: {BASE_PACKAGE} -> {args.package}")
    if args.dry_run:
        print("-- dry run, nothing will be written --")

    total = sum(copy_module(m, target_root, args.package, args.dry_run, args.force) for m in modules)
    total += copy_build_logic(target_root, args.package, args.dry_run, args.force)
    print(f"\n{total} files.")

    print(
        "\nMerge the gradle/libs.versions.toml entries these files reference into the target's "
        "own — copy the [versions]/[libraries]/[bundles]/[plugins] rows this project's catalog "
        "has for them, including every `convention-*` plugin alias."
    )

    print("\nAdd to the target's settings.gradle.kts, inside pluginManagement { }:\n")
    print('    includeBuild("build-logic")')
    print("\nand at the top level:\n")
    print(settings_snippet(modules))
    print(f"\nAdd to the target's gradle.properties:\n\n    basePackage={args.package}")
    print("\nThen create that project's own core/ (theme + Koin) and feature/ modules.")


if __name__ == "__main__":
    main()
