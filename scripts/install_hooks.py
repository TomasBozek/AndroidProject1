#!/usr/bin/env python3
"""
Installs a pre-commit hook that runs the convention checks.

    python3 scripts/install_hooks.py
    python3 scripts/install_hooks.py --uninstall
    python3 scripts/install_hooks.py --dry-run

`doctor.py` is fast and catches exactly the things a hurried commit gets wrong — a ViewModel that
was never registered, a destination missing from `AppNavHost`, a screen missing its seventh file.
CI already gates on it; this just moves the feedback from three minutes away to instant.

The hook is quiet when everything passes and prints the full report when it does not. It also runs
`test_scripts.py`, but only when the commit touches `scripts/` — it takes ~105s, which is too slow
to pay on every commit.

Hooks are not version controlled, so every clone has to run this once.
"""

from __future__ import annotations

import argparse
import subprocess
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))

from _common import REPO_ROOT, relative_to_repo  # noqa: E402

HOOK_NAME = "pre-commit"

MARKER = "# managed by scripts/install_hooks.py"

HOOK = f"""#!/bin/sh
{MARKER}
# Remove with: python3 scripts/install_hooks.py --uninstall
# Skip once with: git commit --no-verify

root="$(git rev-parse --show-toplevel)"

if ! output="$(python3 "$root/scripts/doctor.py" 2>&1)"; then
    echo "$output"
    echo
    echo "doctor.py failed — commit aborted. Fix the above, or use --no-verify."
    exit 1
fi

# Only when the commit touches the generators: the suite copies the repo per test and takes
# roughly twenty seconds, which is too slow to pay on every commit.
if git diff --cached --name-only --diff-filter=ACMR | grep -q '^scripts/'; then
    if ! output="$(python3 "$root/scripts/test_scripts.py" 2>&1)"; then
        echo "$output"
        echo
        echo "test_scripts.py failed — commit aborted. Fix the above, or use --no-verify."
        exit 1
    fi
fi
"""


def hooks_directory() -> Path:
    """Honours core.hooksPath, which silently makes .git/hooks inert when it is set."""
    try:
        configured = subprocess.run(
            ["git", "config", "--get", "core.hooksPath"],
            cwd=REPO_ROOT,
            capture_output=True,
            text=True,
        ).stdout.strip()
    except OSError:
        configured = ""

    if configured:
        path = Path(configured)
        return path if path.is_absolute() else REPO_ROOT / path

    try:
        common = subprocess.run(
            ["git", "rev-parse", "--git-common-dir"],
            cwd=REPO_ROOT,
            capture_output=True,
            text=True,
            check=True,
        ).stdout.strip()
    except (OSError, subprocess.CalledProcessError):
        sys.exit("Not a git repository — nothing to install into.")

    path = Path(common)
    return (path if path.is_absolute() else REPO_ROOT / path) / "hooks"


def install(dry_run: bool) -> None:
    directory = hooks_directory()
    hook = directory / HOOK_NAME

    if hook.is_file() and MARKER not in hook.read_text():
        sys.exit(
            f"{hook} already exists and was not written by this script.\n"
            "Move it aside first — overwriting someone's own hook is not this script's call."
        )

    if dry_run:
        print(f"  would write {hook}")
        return

    directory.mkdir(parents=True, exist_ok=True)
    hook.write_text(HOOK)
    hook.chmod(0o755)
    print(f"  wrote {hook}")
    print("\nInstalled. `git commit` now runs doctor.py first; --no-verify skips it.")


def uninstall(dry_run: bool) -> None:
    hook = hooks_directory() / HOOK_NAME

    if not hook.is_file():
        print("  no pre-commit hook installed")
        return
    if MARKER not in hook.read_text():
        sys.exit(f"{hook} was not written by this script — leaving it alone.")

    if dry_run:
        print(f"  would remove {hook}")
        return
    hook.unlink()
    print(f"  removed {hook}")


def main() -> None:
    parser = argparse.ArgumentParser(
        description="Install or remove this project's git hooks.",
        epilog=(
            'Examples:\n'
            '  python3 scripts/install_hooks.py\n'
            '  python3 scripts/install_hooks.py --uninstall\n'
            '\n'
            'Hooks are not version controlled, so each clone runs this once.'
        ),
        formatter_class=argparse.RawDescriptionHelpFormatter,
    )
    parser.add_argument("--uninstall", action="store_true", help="Remove the hook instead.")
    parser.add_argument("--dry-run", action="store_true", help="Show what would happen, change nothing.")
    args = parser.parse_args()

    if args.uninstall:
        uninstall(args.dry_run)
    else:
        install(args.dry_run)


if __name__ == "__main__":
    main()
