#!/usr/bin/env python3
"""
Copies the reusable `service/` modules into another project.

`service/` is written to be portable: nothing in it references `:core:*`, `:feature:*` or `:app`,
and it reads no `R` but its own. What it cannot do on its own is change its package — the sources
sit in `com.example.androidproject1.core.*` because that is this project's base package — or bring
the version catalog entries its build files rely on. This script does the copy, the package
rewrite and (with `--sync-versions`) the catalog merge in one step.

`build-logic/` comes along with it: the service build files apply the `convention.*` plugins, so
the modules do not compile without it.

    python3 scripts/export_service.py --to ~/Projects/android/MyNewApp --package com.acme.myapp
    python3 scripts/export_service.py --to ~/Projects/android/MyNewApp --sync-versions

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
import tomllib
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))

from _common import (  # noqa: E402
    BASE_PACKAGE,
    BASE_PATH,
    LAYER_SUFFIX,
    REPO_ROOT,
    VERSION_CATALOG_FILE,
)

SERVICE_DIR = REPO_ROOT / "service"
BUILD_LOGIC_DIR = REPO_ROOT / "build-logic"

# Directories that are build output or IDE state rather than source.
SKIP_DIRS = {"build", ".gradle", ".kotlin", ".idea"}

TEXT_SUFFIXES = {".kt", ".kts", ".xml", ".pro", ".md"}

# The order layers are listed in settings.gradle.kts.
SERVICE_LAYER_ORDER = ["domain", "data", "ui", "presentation", "di"]

LAYER_SUFFIXES = {**LAYER_SUFFIX, "ui": "Ui"}

CATALOG_SECTIONS = ["versions", "libraries", "bundles", "plugins"]


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
            '  python3 scripts/export_service.py --to ~/Projects/OtherApp --package com.acme.other --sync-versions\n'
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
    parser.add_argument(
        "--sync-versions",
        action="store_true",
        help="Merge the version catalog entries the copied build files need into the target's "
             "gradle/libs.versions.toml, creating it if needed.",
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
# Version catalog
# --------------------------------------------------------------------------------------------

ACCESSOR = re.compile(r"\blibs\.(?:(plugins|bundles)\.)?([A-Za-z][A-Za-z0-9]*(?:\.[A-Za-z][A-Za-z0-9]*)*)")

# How a convention plugin names the same thing: `libs.findBundle("compose-core")`. Since the shared
# dependencies moved into build-logic/, this is where most of them are declared.
FINDER = re.compile(r'\blibs\.find(Library|Bundle|Plugin)\(\s*"([A-Za-z0-9_.\-]+)"')

FINDER_SECTION = {"Library": "libraries", "Bundle": "bundles", "Plugin": "plugins"}


def catalog_entry_lines(text: str) -> dict[str, dict[str, str]]:
    """
    Maps section -> alias -> the alias's raw source lines.

    Re-emitting the original text rather than serialising the parsed data keeps the target
    catalog's entries byte-identical to this project's, comments in the value included.
    """
    sections: dict[str, dict[str, str]] = {name: {} for name in CATALOG_SECTIONS}
    current: str | None = None
    lines = text.split("\n")
    index = 0

    while index < len(lines):
        line = lines[index]
        header = re.match(r"\s*\[([\w.\-]+)\]\s*$", line)
        if header:
            current = header.group(1)
            sections.setdefault(current, {})
            index += 1
            continue

        entry = re.match(r"\s*([A-Za-z0-9_.\-]+)\s*=", line)
        if entry and current:
            block = [line]
            depth = line.count("[") - line.count("]") + line.count("{") - line.count("}")
            while depth > 0 and index + 1 < len(lines):
                index += 1
                block.append(lines[index])
                depth += lines[index].count("[") - lines[index].count("]")
                depth += lines[index].count("{") - lines[index].count("}")
            sections[current][entry.group(1)] = "\n".join(block)
        index += 1

    return sections


def normalise(alias: str) -> str:
    return alias.replace("-", ".").replace("_", ".")


def accessors(text: str) -> list[tuple[str, str]]:
    """Every catalog reference in a file, as (section, alias-as-written), in either spelling."""
    found = [
        ({"plugins": "plugins", "bundles": "bundles"}.get(kind, "libraries"), accessor)
        for kind, accessor in ACCESSOR.findall(text)
    ]
    found += [(FINDER_SECTION[kind], accessor) for kind, accessor in FINDER.findall(text)]
    return found


def required_catalog_entries(sources: list[Path], catalog: dict) -> dict[str, list[str]]:
    """
    Resolves every `libs.*` accessor used by the copied files into catalog aliases, pulling in the
    version refs they point at and the libraries a bundle is made of.
    """
    lookup = {
        section: {normalise(alias): alias for alias in catalog.get(section, {})}
        for section in CATALOG_SECTIONS
    }
    needed: dict[str, set[str]] = {section: set() for section in CATALOG_SECTIONS}
    unresolved: set[str] = set()

    def add_library(alias: str) -> None:
        needed["libraries"].add(alias)
        version = catalog.get("libraries", {}).get(alias, {})
        if isinstance(version, dict):
            ref = version.get("version", {})
            if isinstance(ref, dict) and "ref" in ref:
                needed["versions"].add(ref["ref"])

    for path in sources:
        for section, accessor in accessors(path.read_text()):
            alias = lookup[section].get(normalise(accessor))
            if alias is None:
                unresolved.add(f"{section}: {accessor}")
                continue

            if section == "plugins":
                needed["plugins"].add(alias)
                ref = catalog.get("plugins", {}).get(alias, {}).get("version", {})
                if isinstance(ref, dict) and "ref" in ref:
                    needed["versions"].add(ref["ref"])
            elif section == "bundles":
                needed["bundles"].add(alias)
                for member in catalog.get("bundles", {}).get(alias, []):
                    add_library(member)
            else:
                add_library(alias)

    if unresolved:
        print(f"  warning: could not resolve {', '.join(sorted(unresolved))} in {VERSION_CATALOG_FILE.name}")

    return {section: sorted(aliases) for section, aliases in needed.items()}


def merge_catalog(target_file: Path, needed: dict[str, list[str]], source_lines: dict[str, dict[str, str]], dry_run: bool) -> None:
    existing_text = target_file.read_text() if target_file.is_file() else ""
    existing = catalog_entry_lines(existing_text) if existing_text else {}

    missing = {
        section: [alias for alias in aliases if alias not in existing.get(section, {})]
        for section, aliases in needed.items()
    }
    conflicts = [
        f"{section}.{alias}"
        for section, aliases in needed.items()
        for alias in aliases
        if alias in existing.get(section, {})
        and existing[section][alias].strip() != source_lines[section][alias].strip()
    ]

    if not any(missing.values()):
        print(f"  version catalog: {target_file} already has every entry")
    else:
        text = existing_text or ""
        for section in CATALOG_SECTIONS:
            aliases = missing[section]
            if not aliases:
                continue
            block = "\n".join(source_lines[section][alias] for alias in aliases)
            header = f"[{section}]"
            # `^` as well as `\n`: the first section starts at the very beginning of the file.
            pattern = re.compile(rf"(^|\n)(\[{section}\]\n)(.*?)(?=\n\[|\Z)", re.DOTALL)
            if pattern.search(text):
                # Append at the end of the existing section, before the next header.
                text = pattern.sub(
                    lambda m: m.group(1) + m.group(2) + m.group(3).rstrip("\n") + "\n" + block + "\n",
                    text,
                    count=1,
                )
            else:
                text = text.rstrip("\n") + f"\n\n{header}\n{block}\n"
            print(f"  version catalog: adding {len(aliases)} entr{'y' if len(aliases) == 1 else 'ies'} to [{section}]")

        if dry_run:
            print(f"  would write {target_file}")
        else:
            target_file.parent.mkdir(parents=True, exist_ok=True)
            target_file.write_text(text.lstrip("\n"))
            print(f"  wrote {target_file}")

    if conflicts:
        print(
            f"  warning: {len(conflicts)} alias(es) already exist in the target with a different "
            f"definition and were left alone: {', '.join(conflicts)}"
        )


def sync_versions(target_root: Path, modules: list[str], dry_run: bool) -> None:
    catalog = tomllib.loads(VERSION_CATALOG_FILE.read_text())
    source_lines = catalog_entry_lines(VERSION_CATALOG_FILE.read_text())
    sources = [
        path
        for module in modules
        for path in iter_source_files(SERVICE_DIR / module)
        if path.name == "build.gradle.kts"
    ]
    # The convention plugins declare most of what the service modules depend on, so scanning only
    # the build files would export a catalog that is missing half of it.
    sources += [path for path in iter_source_files(BUILD_LOGIC_DIR) if path.suffix in {".kts", ".kt"}]
    needed = required_catalog_entries(sources, catalog)
    # Every `convention.*` alias ships with build-logic/, whether or not a service module happens to
    # apply it: the target's own core/ and feature/ modules will want the rest of them.
    needed["plugins"] = sorted(
        set(needed["plugins"]) | {a for a in catalog.get("plugins", {}) if a.startswith("convention-")}
    )
    merge_catalog(target_root / "gradle/libs.versions.toml", needed, source_lines, dry_run)


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
    print(f"Modules: {', '.join(f'{m} ({", ".join(SERVICE_MODULES[m])})' for m in modules)}")
    print(f"Package: {BASE_PACKAGE} -> {args.package}")
    if args.dry_run:
        print("-- dry run, nothing will be written --")

    total = sum(copy_module(m, target_root, args.package, args.dry_run, args.force) for m in modules)
    total += copy_build_logic(target_root, args.package, args.dry_run, args.force)
    print(f"\n{total} files.")

    if args.sync_versions:
        sync_versions(target_root, modules, args.dry_run)
    else:
        print("\nRe-run with --sync-versions to merge the required gradle/libs.versions.toml entries.")

    print("\nAdd to the target's settings.gradle.kts, inside pluginManagement { }:\n")
    print('    includeBuild("build-logic")')
    print("\nand at the top level:\n")
    print(settings_snippet(modules))
    print(f"\nAdd to the target's gradle.properties:\n\n    basePackage={args.package}")
    print("\nThen create that project's own core/ (theme + Koin) and feature/ modules.")


if __name__ == "__main__":
    main()
