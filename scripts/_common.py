"""Shared helpers for the scaffolding scripts."""

from __future__ import annotations

import re
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parent.parent

# Base package of the app. Change this (and the directories on disk) if you rename the package.
BASE_PACKAGE = "com.example.androidproject1"
BASE_PATH = BASE_PACKAGE.replace(".", "/")

TEMPLATE_FEATURE = "example"
TEMPLATE_CLASS = "Example"

SETTINGS_FILE = REPO_ROOT / "settings.gradle.kts"
KOIN_FILE = REPO_ROOT / "core/di/src/main/kotlin" / BASE_PATH / "core/di/Koin.kt"
CORE_DI_BUILD_FILE = REPO_ROOT / "core/di/build.gradle.kts"

# Order matters: this is also the order layers are listed in settings.gradle.kts.
ALL_LAYERS = ["domain", "infrastructure", "data", "presentation", "di"]

LAYER_SUFFIX = {
    "domain": "Domain",
    "infrastructure": "Infrastructure",
    "data": "Data",
    "presentation": "Presentation",
    "di": "Di",
}


def split_words(name: str) -> list[str]:
    """Splits `chatRoom`, `chat_room`, `chat-room` and `ChatRoom` into ['chat', 'room']."""
    spaced = re.sub(r"[_\-\s]+", " ", name)
    spaced = re.sub(r"(?<=[a-z0-9])(?=[A-Z])", " ", spaced)
    return [w.lower() for w in spaced.split() if w]


def to_flat(name: str) -> str:
    """`chatRoom` -> `chatroom`. Used for package and directory names."""
    return "".join(split_words(name))


def to_pascal(name: str) -> str:
    """`chatRoom` -> `ChatRoom`. Used for class names."""
    return "".join(w.capitalize() for w in split_words(name))


def to_camel(name: str) -> str:
    """`ChatRoom` -> `chatRoom`. Used for function names."""
    pascal = to_pascal(name)
    return pascal[:1].lower() + pascal[1:]


def rewrite_source(text: str, flat: str, pascal: str) -> str:
    """
    Rewrites template identifiers to the target feature's.

    The replacements are deliberately narrow rather than a blanket `example` -> `<name>`: the
    base package is `com.example.androidproject1`, so a global replace would corrupt it.
    """
    text = text.replace(f"feature.{TEMPLATE_FEATURE}", f"feature.{flat}")
    text = text.replace(f"feature/{TEMPLATE_FEATURE}", f"feature/{flat}")
    # camelCase identifiers such as `exampleDestination`.
    text = re.sub(rf"\b{TEMPLATE_FEATURE}(?=[A-Z])", flat, text)
    text = text.replace(TEMPLATE_CLASS, pascal)
    return text


def rewrite_relative_path(relative: Path, flat: str, pascal: str) -> Path:
    """
    Maps a path inside the template module to the generated module.

    Only the `.../feature/example/...` package segment is rewritten — the `example` directory in
    `com/example/androidproject1` must be left alone.
    """
    as_posix = relative.as_posix()
    as_posix = as_posix.replace(
        f"{BASE_PATH}/feature/{TEMPLATE_FEATURE}/",
        f"{BASE_PATH}/feature/{flat}/",
    )
    parts = as_posix.split("/")
    parts[-1] = parts[-1].replace(TEMPLATE_CLASS, pascal)
    return Path("/".join(parts))


def write_file(path: Path, content: str, dry_run: bool) -> None:
    if dry_run:
        print(f"  would write {path.relative_to(REPO_ROOT)}")
        return
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(content)
    print(f"  wrote {path.relative_to(REPO_ROOT)}")


def edit_file(path: Path, transform, dry_run: bool, label: str) -> bool:
    """Applies `transform` to a file's text. Returns True if it changed anything."""
    original = path.read_text()
    updated = transform(original)
    if updated == original:
        print(f"  {label}: no change needed")
        return False
    if dry_run:
        print(f"  would update {path.relative_to(REPO_ROOT)} ({label})")
        return True
    path.write_text(updated)
    print(f"  updated {path.relative_to(REPO_ROOT)} ({label})")
    return True
