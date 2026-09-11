#!/usr/bin/env python3
"""
Scaffolds a screen (the six-file Destination/Screen/State/Event/Navigation/ViewModel unit)
into an existing feature, cloned from `feature/template`.

    python3 scripts/create_screen.py userprofile UserProfileList
    python3 scripts/create_screen.py userprofile UserProfileDetail --sub detail
    python3 scripts/create_screen.py userprofile UserProfileList --graph none
    python3 scripts/create_screen.py userprofile UserProfileList --dry-run

The unit lands in a sub-package named after the screen (D34) — `presentation/userprofilelist/` —
so a screen's files sit together and a second screen never forces a move. `--sub` names that
directory instead, which is what a long screen name wants: `--sub search` rather than
`catalogsearch`.

Also clones the template's one feature-local component into the feature's `component/`, renames
the template's string resources into the new screen's namespace and merges them into the
feature's `strings.xml`, registers the ViewModel in the feature's Koin module, and registers the
destination in `AppNavHost.kt`.
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
    TRANSLATED_LOCALES,
    edit_file,
    feature_koin_module_file,
    insert_after_last,
    insert_import,
    merge_strings_xml,
    module_namespace,
    read_string_resources,
    register_destination,
    register_in_features_reference,
    register_route_key_injection,
    rewrite_test_tags,
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

# The args variant of the template screen. Cloned by --with-args instead of the plain one, so the
# generated shape is one that compiles and is checked by doctor.py rather than assembled by string
# surgery here.
TEMPLATE_ARGS_CLASS = "TemplateArgs"
TEMPLATE_ARGS_RESOURCE_PREFIX = "template_args"

# The one argument the args template declares; --with-args rewrites it into the real list.
TEMPLATE_ARG_NAME = "templateId"

# The template's two screens each live in a sub-package named after them (D34).
TEMPLATE_SUB = TEMPLATE_FEATURE
TEMPLATE_ARGS_SUB = f"{TEMPLATE_FEATURE}args"

# The feature-local component the template screen composes. Cloned alongside the screen so that
# what comes out compiles, and so that the first composable a new screen needs already has a home.
TEMPLATE_COMPONENT = f"{TEMPLATE_CLASS}Headline"

SCREEN_SUFFIXES = ["Destination", "Screen", "State", "Event", "Navigation", "ViewModel"]
# Two tests per screen, and they answer different questions: a ViewModel test says what the
# state becomes, a screen test says what is on screen and what a tap does.
TEST_SUFFIXES = ["ViewModelTest", "ScreenTest"]

ARG_TYPES = {
    "String": '"example"',
    "Int": "1",
    "Long": "1L",
    "Boolean": "true",
    "Float": "1f",
    "Double": "1.0",
}

TEMPLATE_TEST_DIR = (
    TEMPLATE_PRESENTATION_DIR / "src/test/kotlin"
    / BASE_PATH / "feature" / TEMPLATE_FEATURE / "presentation"
)

# The template's strings, one file per locale. `create_screen.py` merges the new screen's names
# into every one of them, because doctor.py's translation check reads them all — a screen whose
# Czech was skipped fails the gate at the commit that generated it, not a release later.
TEMPLATE_STRINGS = {
    locale: TEMPLATE_PRESENTATION_DIR / f"src/main/res/{directory}/strings.xml"
    for locale, directory in (("default", "values"), *((code, f"values-{code}") for code in TRANSLATED_LOCALES))
}


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Scaffold a screen inside an existing feature.",
        epilog=(
            'Examples:\n'
            '  python3 scripts/create_screen.py userprofile UserProfileDetail\n'
            "  python3 scripts/create_screen.py userprofile UserProfileDetail --with-args 'userId:String'\n"
            "  python3 scripts/create_screen.py catalog ProductReview --with-args 'productId:String,rating:Int'\n"
            '  python3 scripts/create_screen.py userprofile UserProfileList --sub overview\n'
            '\n'
            'Use --with-args for any screen that takes route arguments; do not hand-convert a data object\n'
            'route into a data class afterwards.'
        ),
        formatter_class=argparse.RawDescriptionHelpFormatter,
    )
    parser.add_argument("feature", help="Existing feature module name, e.g. userprofile")
    parser.add_argument("screen", help="Screen name in PascalCase, e.g. UserProfileList")
    parser.add_argument(
        "--sub",
        default="",
        help="Name the screen's directory instead of deriving it from the screen name, e.g. search",
    )
    parser.add_argument(
        "--graph",
        default="main",
        choices=[*NAV_GRAPHS, "none"],
        help="Nav graph in AppNavHost.kt to register the destination in. Default: main",
    )
    parser.add_argument(
        "--with-args",
        nargs="?",
        const="id:String",
        default=None,
        metavar="SPEC",
        help=(
            "Generate a route that carries arguments, e.g. --with-args 'productId:String,count:Int'. "
            "Bare --with-args means id:String. Types: " + ", ".join(ARG_TYPES) + "."
        ),
    )
    parser.add_argument("--dry-run", action="store_true", help="Show what would happen, change nothing.")
    parser.add_argument("--force", action="store_true", help="Overwrite files that already exist.")
    return parser.parse_args()


def parse_arg_spec(spec: str) -> list[tuple[str, str]]:
    """`productId:String,count:Int` -> [('productId', 'String'), ('count', 'Int')]."""
    arguments = []
    for part in spec.split(","):
        part = part.strip()
        if not part:
            continue
        name, _, type_name = part.partition(":")
        name, type_name = name.strip(), (type_name.strip() or "String")
        if not name.isidentifier():
            sys.exit(f"'{name}' is not a valid argument name.")
        if type_name not in ARG_TYPES:
            sys.exit(f"'{type_name}' is not a supported route argument type. Use one of: {', '.join(ARG_TYPES)}.")
        arguments.append((name, type_name))
    if not arguments:
        sys.exit("--with-args needs at least one argument, e.g. --with-args 'productId:String'.")
    return arguments


def rewrite_arguments(text: str, arguments: list[tuple[str, str]]) -> str:
    """
    Expands the template's single `templateId: String` into the real argument list.

    Three places carry it: the route's constructor, the SavedStateHandle map in the test, and every
    reference to `args.templateId`. The first argument stands in for the template's one, so those
    references keep working; the rest are appended.
    """
    first_name, first_type = arguments[0]

    # The route's parameter list, written on one line in the template.
    declaration = ", ".join(f"val {name}: {type_name}" for name, type_name in arguments)
    text = text.replace(f"val {TEMPLATE_ARG_NAME}: String)", f"{declaration})")

    # The test's SavedStateHandle map.
    entries = ", ".join(f'"{name}" to {ARG_TYPES[type_name]}' for name, type_name in arguments)
    text = text.replace(f'mapOf("{TEMPLATE_ARG_NAME}" to "example")', f"mapOf({entries})")

    # Everything else — the state field, `args.templateId`, the preview fixture — follows the first.
    text = text.replace(TEMPLATE_ARG_NAME, first_name)
    if first_type != "String":
        text = text.replace(f'val {first_name}: String', f"val {first_name}: {first_type}")
        text = text.replace(f'{first_name} = "example"', f"{first_name} = {ARG_TYPES[first_type]}")
    return text


def template_string_entries(
    source: Path,
    screen_pascal: str,
    screen_snake: str,
    resource_prefix: str,
    class_name: str,
) -> dict[str, str]:
    """
    Maps the template's `template_*` strings onto the new screen's `<snake>_*` names.

    Without this a generated screen references `R.string.template_title`, which only exists in the
    template feature — and two screens in one feature would fight over the same name.

    `source` is one locale's file. The rename is by resource name, which every locale shares, so
    the same mapping runs over `values/` and `values-cs/` alike.
    """
    entries = {}
    for name, value in read_string_resources(source).items():
        if not name.startswith(f"{resource_prefix}_"):
            continue
        # `template_args_title` must not match the plain `template_` prefix as well.
        if resource_prefix == TEMPLATE_RESOURCE_PREFIX and name.startswith(f"{TEMPLATE_ARGS_RESOURCE_PREFIX}_"):
            continue
        suffix = name[len(resource_prefix) + 1:]
        # The value is prose ("Template screen for %1$s"), so it carries the plain class name even
        # in the args template. Replace the specific name first, then whatever is left.
        entries[f"{screen_snake}_{suffix}"] = value.replace(class_name, screen_pascal).replace(
            TEMPLATE_CLASS, screen_pascal
        )
    return entries


def rewrite(
    text: str,
    feature: str,
    screen_pascal: str,
    screen_camel: str,
    screen_snake: str,
    sub_package: str,
    r_import: str,
    with_args: bool = False,
) -> str:
    # The template screen's own directory (D34) comes off first: the generated screen gets its
    # own below, and the `R` import is re-derived from the module's namespace rather than carried
    # over — a module that sets `namespace` itself would otherwise import the wrong one.
    text = re.sub(
        rf"(\.presentation)\.(?:{TEMPLATE_ARGS_SUB}|{TEMPLATE_SUB})\b",
        r"\1",
        text,
    )
    text = text.replace(f"import {BASE_PACKAGE}.feature.{TEMPLATE_FEATURE}.presentation.R\n", "")

    # The feature's package segment.
    text = text.replace(f"feature.{TEMPLATE_FEATURE}", f"feature.{feature}")

    # Tags first. A tag's stem is camelCase and a resource's is snake_case, so the rules below
    # would turn `template_saveButton` into `product_review_saveButton` and `templateArgs_idValue`
    # into `productReviewArgs_idValue` — both wrong, and both silent.
    text = rewrite_test_tags(text, screen_camel)

    # `TemplateArgs` and `templateArgs` first: replacing the shorter `Template` prefix ahead of
    # them would leave `ProductDetailArgsViewModel` behind.
    if with_args:
        text = re.sub(rf"\b{TEMPLATE_FEATURE}Args(?=[A-Z])", screen_camel, text)
        text = text.replace(TEMPLATE_ARGS_CLASS, screen_pascal)
        # ...and the same for resource names, before the plain `template_` rule below.
        text = re.sub(rf"\b{TEMPLATE_ARGS_RESOURCE_PREFIX}_(\w+)", rf"{screen_snake}_\1", text)

    # camelCase identifiers derived from the screen, e.g. `templateDestination`.
    text = re.sub(rf"\b{TEMPLATE_FEATURE}(?=[A-Z])", screen_camel, text)
    # Class names.
    text = text.replace(TEMPLATE_CLASS, screen_pascal)
    # `R.string.template_title` -> `R.string.user_profile_list_title`.
    text = re.sub(rf"\b{TEMPLATE_RESOURCE_PREFIX}_(\w+)", rf"{screen_snake}_\1", text)

    if sub_package:
        # Anchored, so that a file already in a package of its own — `component/` — keeps it.
        text = re.sub(
            rf"^package {re.escape(BASE_PACKAGE)}\.feature\.{feature}\.presentation$",
            f"package {BASE_PACKAGE}.feature.{feature}.presentation.{sub_package}",
            text,
            count=1,
            flags=re.MULTILINE,
        )

    # R lives in the module's namespace package, and since D34 nothing generated sits in it.
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
    # D34: a screen lives in a directory named after it unless --sub names a different one.
    sub_path = args.sub.strip("/") or to_flat(args.screen)
    sub_package = sub_path.replace("/", ".")
    arguments = parse_arg_spec(args.with_args) if args.with_args else None

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

    # (template file, where it goes, what it is called there). The seventh and eighth files are
    # the tests, which live in the test source set — so a generated screen starts testable rather
    # than becoming testable later. Named explicitly rather than globbed: `Template*.kt` matches
    # the args variant too, and a glob would clone both sets into one screen.
    class_name = TEMPLATE_ARGS_CLASS if arguments else TEMPLATE_CLASS
    template_sub = TEMPLATE_ARGS_SUB if arguments else TEMPLATE_SUB
    component_dir = (
        feature_presentation / "src/main/kotlin"
        / BASE_PATH / "feature" / feature / "presentation" / "component"
    )
    sources = [
        (TEMPLATE_DIR / template_sub / f"{class_name}{suffix}.kt", dest_dir, f"{screen_pascal}{suffix}.kt")
        for suffix in SCREEN_SUFFIXES
    ]
    sources += [
        (TEMPLATE_TEST_DIR / template_sub / f"{class_name}{suffix}.kt", screen_dir("test"), f"{screen_pascal}{suffix}.kt")
        for suffix in TEST_SUFFIXES
    ]
    # The one feature-local composable the screen composes (D34). It carries the plain class name
    # even in the args variant, so its target name is derived from that rather than from
    # `class_name`.
    sources += [
        (
            TEMPLATE_DIR / "component" / f"{TEMPLATE_COMPONENT}.kt",
            component_dir,
            f"{TEMPLATE_COMPONENT.replace(TEMPLATE_CLASS, screen_pascal)}.kt",
        )
    ]

    missing = [s for s, _, _ in sources if not s.is_file()]
    if missing:
        sys.exit("Template files are missing:\n  " + "\n  ".join(str(m) for m in missing))

    print(f"Creating screen '{screen_pascal}' in feature '{feature}' (strings: {screen_snake}_xxx)")
    if arguments:
        print("Route arguments: " + ", ".join(f"{n}: {ty}" for n, ty in arguments))
    print(f"Target: {dest_dir.relative_to(REPO_ROOT)}")
    if args.dry_run:
        print("-- dry run, nothing will be written --")

    wrote_any = False
    for source, target_dir, name in sources:
        dest = target_dir / name
        if dest.exists() and not args.force:
            print(f"  skipping {dest.relative_to(REPO_ROOT)}: already exists (use --force)")
            continue
        text = source.read_text()
        # Before rewrite(), not after: its camelCase rule turns `templateId` into
        # `productReviewId`, and rewrite_arguments would then have nothing left to match.
        if arguments:
            text = rewrite_arguments(text, arguments)
        text = rewrite(
            text,
            feature,
            screen_pascal,
            screen_camel,
            screen_snake,
            # A component is already in a package of its own; only the screen's six files and
            # their tests move into the screen's directory.
            "" if target_dir == component_dir else sub_package,
            r_import,
            with_args=bool(arguments),
        )
        write_file(dest, text, args.dry_run)
        wrote_any = True

    if not wrote_any:
        sys.exit("Nothing was generated.")

    for locale, template_strings in TEMPLATE_STRINGS.items():
        directory = "values" if locale == "default" else f"values-{locale}"
        merge_strings_xml(
            feature_presentation / f"src/main/res/{directory}/strings.xml",
            template_string_entries(
                template_strings,
                screen_pascal,
                screen_snake,
                TEMPLATE_ARGS_RESOURCE_PREFIX if arguments else TEMPLATE_RESOURCE_PREFIX,
                TEMPLATE_ARGS_CLASS if arguments else TEMPLATE_CLASS,
            ),
            args.dry_run,
        )

    register_in_koin(feature, screen_pascal, sub_package, args.dry_run)

    package_suffix = f".{sub_package}" if sub_package else ""
    register_destination(
        import_line=f"import {BASE_PACKAGE}.feature.{feature}.presentation{package_suffix}.{screen_camel}Destination",
        call_line=f"{screen_camel}Destination(backStack = backStack)",
        graph=args.graph,
        dry_run=args.dry_run,
    )

    if arguments:
        register_route_key_injection(feature, screen_pascal, sub_package, args.dry_run)

    register_in_features_reference(
        screen_pascal,
        feature,
        ", ".join(f"`{name}`" for name, _ in arguments or []),
        args.dry_run,
    )

    print(
        "\nDone. Fill in the placeholders, then run the T0 pair — never `./gradlew build`, which\n"
        "assembles every variant and runs R8 three times (CLAUDE.md \u00a7 Checks):\n"
        f"  python3 scripts/doctor.py && ./gradlew :feature:{feature}:presentation:assembleDebug test"
    )


if __name__ == "__main__":
    main()
