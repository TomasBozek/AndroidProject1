#!/usr/bin/env python3
"""
Turns this template into a named project, in place.

    python3 scripts/init_project.py --package com.acme.tracker --name "Field Tracker"
    python3 scripts/init_project.py --package com.acme.tracker --name "Field Tracker" --dry-run

Run it once, on a fresh clone, before writing any code of your own. It rewrites the base package
across every source file, moves the package directories on disk, renames the Gradle project, the
Android theme and the app label, and updates `scripts/_common.py` so the other generators keep
working afterwards.

This is the one script that edits the whole repository rather than adding to it, so it refuses to
run on a dirty working tree: `git checkout .` has to stay a usable escape hatch. Pass `--force` if
you know better.
"""

from __future__ import annotations

import argparse
import re
import subprocess
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))

from _common import (  # noqa: E402
    BASE_PACKAGE,
    BASE_PATH,
    REPO_ROOT,
    relative_to_repo,
)

# The project's own name, as opposed to its package. Appears in `rootProject.name`, the Android
# theme, the launcher label and the README title.
PROJECT_NAME = "AndroidProject1"

TEXT_SUFFIXES = {
    ".kt", ".kts", ".xml", ".toml", ".py", ".md", ".pro", ".yml", ".yaml", ".properties",
    # build-logic/compose-stability.conf names the domain packages it vouches for.
    ".conf",
}

SKIP_DIRS = {".git", "build", ".gradle", ".idea", ".kotlin", "__pycache__", ".cxx", "venv", ".venv"}

# This script names the template explicitly in its own help and constants; rewriting it would
# leave it unable to rename anything a second time.
SKIP_FILES = {"init_project.py"}

# Every module here keeps its sources in `src/<set>/kotlin`; `java` is matched too so a source set
# added later in the other layout is still moved rather than silently left behind.
SOURCE_ROOT = re.compile(r"src/[^/]+/(kotlin|java)$")


# --------------------------------------------------------------------------------------------
# Naming
# --------------------------------------------------------------------------------------------


def validate_package(package: str) -> None:
    if not re.fullmatch(r"[a-z][a-z0-9_]*(\.[a-z][a-z0-9_]*)+", package):
        sys.exit(
            f"'{package}' is not a valid lowercase Java package "
            "(expected something like com.acme.myapp)."
        )
    if package == BASE_PACKAGE:
        sys.exit(f"'{package}' is already this project's package — nothing to do.")


def to_gradle_name(display_name: str) -> str:
    """`Field Tracker` -> `FieldTracker`: safe in rootProject.name and an Android theme name."""
    cleaned = re.sub(r"[^0-9A-Za-z ]+", " ", display_name)
    parts = [p for p in cleaned.split() if p]
    if not parts:
        sys.exit(f"'{display_name}' has no letters or digits to build a project name from.")
    name = "".join(p[:1].upper() + p[1:] for p in parts)
    if name[0].isdigit():
        sys.exit(f"'{name}' starts with a digit, which an Android theme name cannot.")
    return name


# --------------------------------------------------------------------------------------------
# Safety
# --------------------------------------------------------------------------------------------


def require_clean_tree(force: bool) -> None:
    if force:
        return
    try:
        result = subprocess.run(
            ["git", "status", "--porcelain"],
            cwd=REPO_ROOT,
            capture_output=True,
            text=True,
            check=True,
        )
    except (OSError, subprocess.CalledProcessError):
        print("  not a git repository — skipping the clean-tree check")
        return

    if result.stdout.strip():
        sys.exit(
            "The working tree has uncommitted changes.\n"
            "This script rewrites every source file in place, so commit first — that keeps\n"
            "`git checkout .` available if the result is not what you wanted.\n"
            "Re-run with --force to proceed anyway."
        )


# --------------------------------------------------------------------------------------------
# Rewriting
# --------------------------------------------------------------------------------------------


def text_files():
    for path in sorted(REPO_ROOT.rglob("*")):
        if not path.is_file() or path.suffix not in TEXT_SUFFIXES:
            continue
        relative = path.relative_to(REPO_ROOT)
        if SKIP_DIRS.intersection(relative.parts) or path.name in SKIP_FILES:
            continue
        yield path


def rewrite_text(text: str, package: str, gradle_name: str, display_name: str) -> str:
    """
    A blanket replacement, and safely so: the point here is to move the whole base package, and
    neither `com.example.androidproject1` nor `AndroidProject1` means anything else in this repo.

    Both forms of the package have to go: the dotted one in `package`/`import` statements and
    Gradle namespaces, and the slash-separated one that appears in path constants — `test_scripts.py`
    hardcodes `com/example/androidproject1`, and missing it leaves the script suite looking in a
    directory the rename just emptied.

    The launcher label and the README title want the human-readable name rather than the Gradle
    one, so those two are put back afterwards.
    """
    text = text.replace(BASE_PACKAGE, package)
    text = text.replace(BASE_PATH, package.replace(".", "/"))
    text = text.replace(PROJECT_NAME, gradle_name)

    text = text.replace(
        f'<string name="app_name">{gradle_name}</string>',
        f'<string name="app_name">{display_name}</string>',
    )
    # The launcher label lives in gradle.properties, one line, because the flavors compose their
    # own labels from it. PROJECT_NAME above has already turned it into the Gradle name.
    text = text.replace(f"appName={gradle_name}", f"appName={display_name}")
    if text.startswith(f"# {gradle_name}\n"):
        text = text.replace(f"# {gradle_name}\n", f"# {display_name}\n", 1)
    return text


def rewrite_files(package: str, gradle_name: str, display_name: str, dry_run: bool) -> int:
    changed = 0
    for path in text_files():
        original = path.read_text()
        updated = rewrite_text(original, package, gradle_name, display_name)
        if updated == original:
            continue
        changed += 1
        if dry_run:
            print(f"  would rewrite {relative_to_repo(path)}")
        else:
            path.write_text(updated)
    if not dry_run:
        print(f"  rewrote {changed} files")
    return changed


# --------------------------------------------------------------------------------------------
# Moving the package directories
# --------------------------------------------------------------------------------------------


def source_roots() -> list[Path]:
    """Every `src/<set>/<kotlin|java>` directory that holds the base package."""
    roots = []
    for path in sorted(REPO_ROOT.rglob("*")):
        if not path.is_dir():
            continue
        relative = path.relative_to(REPO_ROOT)
        if SKIP_DIRS.intersection(relative.parts):
            continue
        if SOURCE_ROOT.search(relative.as_posix()) and (path / BASE_PATH).is_dir():
            roots.append(path)
    return roots


def prune_empty(directory: Path, stop_at: Path) -> None:
    """Removes the now-empty `com/example/...` shell left behind by the move."""
    current = directory
    while current != stop_at and current.is_dir() and not any(current.iterdir()):
        current.rmdir()
        current = current.parent


def move_packages(package: str, dry_run: bool) -> int:
    target_path = package.replace(".", "/")
    moved = 0
    for root in source_roots():
        source = root / BASE_PATH
        destination = root / target_path
        if dry_run:
            print(f"  would move {relative_to_repo(source)} -> {relative_to_repo(destination)}")
            moved += 1
            continue

        destination.parent.mkdir(parents=True, exist_ok=True)
        if destination.exists():
            sys.exit(f"{relative_to_repo(destination)} already exists — refusing to overwrite it.")
        source.rename(destination)
        prune_empty(source.parent, root)
        moved += 1
    if not dry_run:
        print(f"  moved {moved} package directories")
    return moved


# --------------------------------------------------------------------------------------------


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Rename this template into a project of your own, in place.",
        epilog=(
            "Example:\n"
            "  python3 scripts/init_project.py --package com.acme.tracker --name 'Field Tracker'"
        ),
        formatter_class=argparse.RawDescriptionHelpFormatter,
    )
    parser.add_argument("--package", required=True, help="New base package, e.g. com.acme.tracker")
    parser.add_argument(
        "--name",
        required=True,
        help="Display name shown under the launcher icon, e.g. 'Field Tracker'",
    )
    parser.add_argument(
        "--gradle-name",
        default=None,
        help="Override the Gradle/theme name derived from --name (default: FieldTracker)",
    )
    parser.add_argument("--dry-run", action="store_true", help="Show what would happen, change nothing.")
    parser.add_argument(
        "--force",
        action="store_true",
        help="Run even with uncommitted changes in the working tree.",
    )
    return parser.parse_args()


def main() -> None:
    args = parse_args()

    validate_package(args.package)
    display_name = args.name.strip()
    gradle_name = args.gradle_name or to_gradle_name(display_name)

    print(f"Package:      {BASE_PACKAGE} -> {args.package}")
    print(f"Project name: {PROJECT_NAME} -> {gradle_name}")
    print(f"App label:    {display_name}")
    if args.dry_run:
        print("-- dry run, nothing will be written --")
    else:
        require_clean_tree(args.force)

    print("\nRewriting sources")
    rewrite_files(args.package, gradle_name, display_name, args.dry_run)

    print("\nMoving package directories")
    move_packages(args.package, args.dry_run)

    if args.dry_run:
        print("\nDry run complete. Re-run without --dry-run to apply.")
        return

    print(
        "\nDone. Now:\n"
        "  python3 scripts/doctor.py && ./gradlew :app:assembleDevDebug test\n"
        "\n"
        "Then replace the sample features with your own:\n"
        "  python3 scripts/delete_feature.py catalog\n"
        "  python3 scripts/create_feature.py yourFeature\n"
        "\n"
        "`feature/template` is the generator source — keep it."
    )


if __name__ == "__main__":
    main()
