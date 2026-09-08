#!/usr/bin/env python3
"""
Scaffolds a data source (and optionally the repository above it) across the three modules it lives
in, plus the Koin bindings.

    python3 scripts/create_datasource.py userprofile LocalUserProfile
    python3 scripts/create_datasource.py userprofile LocalUserProfile --repository
    python3 scripts/create_datasource.py userprofile RemoteUserProfile --repository UserProfile --dry-run

The point is the layer inversion, which is the thing most often got backwards when written by
hand: the `XDataSource` *interface* belongs to `gateway` and `DefaultXDataSource` to `data`,
so `data` depends on `gateway` and not the other way round. `--repository` adds the matching
`XRepository` in `domain` and `DefaultXRepository` in `gateway`.
"""

from __future__ import annotations

import argparse
import re
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))

from _common import (  # noqa: E402
    BASE_PACKAGE,
    edit_file,
    feature_koin_module_file,
    feature_module_dir,
    feature_package,
    feature_source_dir,
    insert_import,
    to_camel,
    to_flat,
    to_pascal,
    to_snake,
    write_file,
)

# `LocalUserProfile` -> repository `UserProfile`, matching `LocalAuthDataSource` / `AuthRepository`.
SOURCE_QUALIFIERS = ("Local", "Remote", "Cached", "InMemory")


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Scaffold a data source across gateway/data/di.")
    parser.add_argument("feature", help="Existing feature module name, e.g. userprofile")
    parser.add_argument("name", help="Data source name without the suffix, e.g. LocalUserProfile")
    parser.add_argument(
        "--repository",
        nargs="?",
        const="",
        default=None,
        metavar="NAME",
        help="Also generate a repository. Defaults to the data source name without its "
             f"{'/'.join(SOURCE_QUALIFIERS)} qualifier.",
    )
    parser.add_argument("--dry-run", action="store_true", help="Show what would happen, change nothing.")
    parser.add_argument("--force", action="store_true", help="Overwrite files that already exist.")
    return parser.parse_args()


def strip_suffix(name: str, suffix: str) -> str:
    return name[: -len(suffix)] if name.endswith(suffix) else name


def default_repository_name(source: str) -> str:
    for qualifier in SOURCE_QUALIFIERS:
        if source.startswith(qualifier) and len(source) > len(qualifier):
            return source[len(qualifier):]
    return source


def data_source_interface(flat: str, source: str) -> str:
    return f"""package {feature_package(flat, "gateway")}

import kotlinx.coroutines.flow.Flow

/**
 * Declared in gateway, implemented in `:feature:{flat}:data`. This inversion is what keeps
 * the data layer depending on gateway rather than the other way round.
 */
interface {source}DataSource {{

    // TODO: replace with the real operations.
    fun observeValue(): Flow<String?>

    suspend fun setValue(value: String?)
}}
"""


def data_source_implementation(flat: str, source: str, key: str) -> str:
    return f"""package {feature_package(flat, "data")}

import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import {BASE_PACKAGE}.core.data.DataStoreProvider
import {feature_package(flat, "gateway")}.{source}DataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class Default{source}DataSource(
    dataStoreProvider: DataStoreProvider,
) : {source}DataSource {{

    private val dataStore = dataStoreProvider.dataStore

    override fun observeValue(): Flow<String?> = dataStore.data.map {{ it[KEY_VALUE] }}

    override suspend fun setValue(value: String?) {{
        dataStore.edit {{ preferences ->
            if (value == null) preferences.remove(KEY_VALUE) else preferences[KEY_VALUE] = value
        }}
    }}

    private companion object {{

        val KEY_VALUE = stringPreferencesKey("{key}_value")
    }}
}}
"""


def repository_interface(flat: str, repository: str) -> str:
    return f"""package {feature_package(flat, "domain")}

import {BASE_PACKAGE}.core.domain.result.Outcome
import kotlinx.coroutines.flow.Flow

/**
 * Implemented in the gateway layer. Declared here so the domain layer depends on nothing.
 */
interface {repository}Repository {{

    // TODO: replace with the real operations.
    fun observeValue(): Flow<Outcome<String?>>

    suspend fun setValue(value: String): Outcome<Unit>
}}
"""


def repository_implementation(flat: str, repository: str, source: str) -> str:
    field = to_camel(source) + "DataSource"
    return f"""package {feature_package(flat, "gateway")}

import {BASE_PACKAGE}.core.data.BaseRepository
import {BASE_PACKAGE}.core.domain.Logger
import {BASE_PACKAGE}.core.domain.result.Outcome
import {feature_package(flat, "domain")}.{repository}Repository
import kotlinx.coroutines.flow.Flow

class Default{repository}Repository(
    logger: Logger,
    private val {field}: {source}DataSource,
) : {repository}Repository, BaseRepository(logger = logger.withTag("Default{repository}Repository")) {{

    // Pass `retries` when the collector outlives the failure — a flow that has thrown is terminal
    // and can only be resubscribed, not resumed.
    override fun observeValue(): Flow<Outcome<String?>> =
        observe(source = {field}.observeValue())

    override suspend fun setValue(value: String): Outcome<Unit> = execute {{
        {field}.setValue(value)
    }}
}}
"""


def register_in_koin(flat: str, bindings: list[tuple[str, str, str]], dry_run: bool) -> None:
    """`bindings` is a list of (implementation, interface, package of the interface)."""
    module_file = feature_koin_module_file(flat)
    if module_file is None:
        print(f"  no Koin module found under feature/{flat}/di — register the bindings manually")
        return

    def transform(text: str) -> str:
        lines = text.split("\n")
        added = False

        for implementation, interface, interface_package in bindings:
            entry = f"singleOf(::{implementation}) bind {interface}::class"
            if entry in text:
                continue
            added = True

            insert_import(lines, f"import {interface_package}.{interface}")
            insert_import(lines, f"import {_implementation_package(flat, implementation)}.{implementation}")
            insert_import(lines, "import org.koin.core.module.dsl.singleOf")
            insert_import(lines, "import org.koin.dsl.bind")

            _insert_binding(lines, entry)

        return "\n".join(lines) if added else text

    edit_file(module_file, transform, dry_run, "register Koin bindings")


def _implementation_package(flat: str, implementation: str) -> str:
    """`DefaultXDataSource` lives in `data`, `DefaultXRepository` in `gateway`."""
    return feature_package(flat, "data" if implementation.endswith("DataSource") else "gateway")


def _insert_binding(lines: list[str], entry: str) -> None:
    """Appends the binding after the last existing one, else after the ViewModels, else after `module {`."""
    for pattern, offset in (
        (re.compile(r"^(\s*)singleOf\(.*\)( bind .*)?$"), 1),
        (re.compile(r"^(\s*)viewModelOf\(.*\)$"), 2),
    ):
        matches = [i for i, line in enumerate(lines) if pattern.match(line)]
        if matches:
            anchor = matches[-1]
            indent = pattern.match(lines[anchor]).group(1)
            # A blank line between the ViewModel block and the bindings, as in AuthModule.
            block = [""] * (offset - 1) + [f"{indent}{entry}"]
            lines[anchor + 1: anchor + 1] = block
            return

    anchor = next(i for i, line in enumerate(lines) if line.rstrip().endswith("= module {"))
    lines.insert(anchor + 1, f"{' ' * 8}{entry}")


def require_layer(flat: str, layer: str) -> None:
    if not feature_module_dir(flat, layer).is_dir():
        sys.exit(
            f"feature/{flat}/{layer} does not exist. Generate it first:\n"
            f"  python3 scripts/create_feature.py {flat} --layers {layer} --force"
        )


def emit(path: Path, content: str, force: bool, dry_run: bool) -> None:
    if path.exists() and not force:
        print(f"  skipping {path.name}: already exists (use --force)")
        return
    write_file(path, content, dry_run)


def main() -> None:
    args = parse_args()

    flat = to_flat(args.feature)
    source = to_pascal(strip_suffix(args.name, "DataSource"))
    wants_repository = args.repository is not None
    repository = to_pascal(strip_suffix(args.repository, "Repository")) if args.repository else default_repository_name(source)

    if not (feature_module_dir(flat, "presentation").is_dir() or feature_module_dir(flat, "domain").is_dir()):
        sys.exit(f"No such feature: feature/{flat}")

    require_layer(flat, "gateway")
    require_layer(flat, "data")
    if wants_repository:
        require_layer(flat, "domain")

    print(f"Creating {source}DataSource in feature '{flat}'")
    if wants_repository:
        print(f"With repository {repository}Repository")
    if args.dry_run:
        print("-- dry run, nothing will be written --")

    emit(
        feature_source_dir(flat, "gateway") / f"{source}DataSource.kt",
        data_source_interface(flat, source),
        args.force,
        args.dry_run,
    )
    emit(
        feature_source_dir(flat, "data") / f"Default{source}DataSource.kt",
        data_source_implementation(flat, source, to_snake(source)),
        args.force,
        args.dry_run,
    )

    bindings = [(f"Default{source}DataSource", f"{source}DataSource", feature_package(flat, "gateway"))]

    if wants_repository:
        emit(
            feature_source_dir(flat, "domain") / f"{repository}Repository.kt",
            repository_interface(flat, repository),
            args.force,
            args.dry_run,
        )
        emit(
            feature_source_dir(flat, "gateway") / f"Default{repository}Repository.kt",
            repository_implementation(flat, repository, source),
            args.force,
            args.dry_run,
        )
        # Registered before the data source so the Koin module reads top-down: repository, source.
        bindings.insert(0, (f"Default{repository}Repository", f"{repository}Repository", feature_package(flat, "domain")))

    register_in_koin(flat, bindings, args.dry_run)

    print("\nDone. Run ./gradlew build")


if __name__ == "__main__":
    main()
