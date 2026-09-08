#!/usr/bin/env python3
"""
Scaffolds a Compose component with its preview.

    python3 scripts/create_component.py PrimaryButton
    python3 scripts/create_component.py ProductCard --feature catalog
    python3 scripts/create_component.py EmptyState --state
    python3 scripts/create_component.py Badge --feature catalog --sub product --dry-run

With no `--feature` the component lands in `:core:ui`, where every feature can reach it. Pass
`--feature` for one that belongs to a single feature and should not be shared.

Unlike `create_feature.py` and `create_screen.py`, this generates from templates held in this file
rather than cloning `feature/template` — the same choice `create_datasource.py` makes. A component
has no counterpart in the template module, and adding a fake one there would ship a placeholder
component in the app.

What it produces is deliberately unopinionated: a `modifier` parameter first (the convention
`doctor.py` enforces), a `@ComponentPreview`, and a TODO where the real content goes.
"""

from __future__ import annotations

import argparse
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))

from _common import (  # noqa: E402
    BASE_PACKAGE,
    BASE_PATH,
    REPO_ROOT,
    feature_source_dir,
    to_flat,
    to_pascal,
    write_file,
)

CORE_UI_DIR = REPO_ROOT / "core/ui/src/main/kotlin" / BASE_PATH / "core/ui"
CORE_UI_PACKAGE = f"{BASE_PACKAGE}.core.ui"

PREVIEW_PACKAGE = f"{BASE_PACKAGE}.core.ui.common"


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Scaffold a Compose component and its preview.",
        epilog=(
            "Examples:\n"
            "  python3 scripts/create_component.py PrimaryButton\n"
            "  python3 scripts/create_component.py ProductCard --feature catalog --state"
        ),
        formatter_class=argparse.RawDescriptionHelpFormatter,
    )
    parser.add_argument("name", help="Component name in PascalCase, e.g. PrimaryButton")
    parser.add_argument(
        "--feature",
        default=None,
        help="Put it in this feature's presentation module instead of :core:ui",
    )
    parser.add_argument("--sub", default="", help="Optional sub-package, e.g. product")
    parser.add_argument(
        "--state",
        action="store_true",
        help="Also generate a <Name>State data class with a PREVIEW fixture, and take it as a parameter",
    )
    parser.add_argument("--dry-run", action="store_true", help="Show what would happen, change nothing.")
    parser.add_argument("--force", action="store_true", help="Overwrite files that already exist.")
    return parser.parse_args()


def resolve_target(args: argparse.Namespace) -> tuple[Path, str]:
    """Returns the directory to write into and the package to declare."""
    if args.feature:
        feature = to_flat(args.feature)
        directory = feature_source_dir(feature, "presentation")
        if not directory.is_dir():
            sys.exit(
                f"No presentation module at feature/{feature}/presentation. "
                f"Create the feature first: python3 scripts/create_feature.py {feature}"
            )
        package = f"{BASE_PACKAGE}.feature.{feature}.presentation"
    else:
        directory = CORE_UI_DIR / "component"
        package = f"{CORE_UI_PACKAGE}.component"

    sub = args.sub.strip("/")
    if sub:
        directory = directory / sub
        package = f"{package}.{sub.replace('/', '.')}"
    return directory, package


def state_source(package: str, pascal: str) -> str:
    return f"""package {package}

import androidx.compose.runtime.Immutable

/**
 * What {pascal} renders. Immutable so Compose can skip it when nothing has changed.
 */
@Immutable
data class {pascal}State(
    // TODO: replace with this component's real state.
    val label: String,
) {{

    companion object {{

        val PREVIEW = {pascal}State(
            label = "{pascal}",
        )
    }}
}}
"""


def component_source(package: str, pascal: str, with_state: bool) -> str:
    parameter = f"state: {pascal}State" if with_state else "label: String"
    body_text = "state.label" if with_state else "label"
    preview_argument = f"state = {pascal}State.PREVIEW" if with_state else f'label = "{pascal}"'

    return f"""package {package}

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import {PREVIEW_PACKAGE}.ComponentPreview
import {PREVIEW_PACKAGE}.ThemedComponentPreview

/**
 * TODO: say what this component shows and when to reach for it.
 *
 * `modifier` comes second and defaults to `Modifier` so the caller decides where this sits — the
 * convention `doctor.py` enforces. Keep it, and pass it to the outermost element.
 */
@Composable
fun {pascal}(
    {parameter},
    modifier: Modifier = Modifier,
) {{
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {{
        Text(
            text = {body_text},
            style = MaterialTheme.typography.bodyMedium,
        )
    }}
}}

@ComponentPreview
@Composable
private fun Preview() = ThemedComponentPreview {{
    {pascal}({preview_argument})
}}
"""


def emit(path: Path, content: str, force: bool, dry_run: bool) -> bool:
    if path.exists() and not force:
        print(f"  skipping {path.relative_to(REPO_ROOT)}: already exists (use --force)")
        return False
    write_file(path, content, dry_run)
    return True


def main() -> None:
    args = parse_args()

    pascal = to_pascal(args.name)
    directory, package = resolve_target(args)

    location = "core/ui" if not args.feature else f"feature/{to_flat(args.feature)}/presentation"
    print(f"Creating component '{pascal}' in {location}")
    print(f"Package: {package}")
    if args.dry_run:
        print("-- dry run, nothing will be written --")

    wrote = False
    if args.state:
        wrote |= emit(
            directory / f"{pascal}State.kt",
            state_source(package, pascal),
            args.force,
            args.dry_run,
        )
    wrote |= emit(
        directory / f"{pascal}.kt",
        component_source(package, pascal, args.state),
        args.force,
        args.dry_run,
    )

    if not wrote:
        sys.exit("Nothing was generated.")

    print(
        "\nDone. A component needs no registration — no Koin binding, no nav entry.\n"
        "Replace the placeholder content, then: ./gradlew build"
    )


if __name__ == "__main__":
    main()
