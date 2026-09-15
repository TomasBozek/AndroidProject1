#!/usr/bin/env python3
"""Print the board as one JSON document, for the board artifact.

Reads docs/STATUS.md, docs/BACKLOG.md, docs/CHANGELOG.md and docs/ai/plans/*.md — the files that
are the truth — and prints one object: the open sprint and its board, the drafts in queue order,
the open release with its done sprints, the product and DevOps backlogs grouped, and what shipped.
A parser and nothing else: no network, no writes. `/board` hands the output to the artifact
(docs/ai/PROCESS.md § Sprints and releases, D67).

Examples:
    python3 scripts/board.py > /tmp/board.json
    python3 scripts/board.py --dry-run          # parse and report counts, print no JSON

Fails on a header line it cannot parse rather than publishing half a board.
"""
from __future__ import annotations

import argparse
import json
import re
import subprocess
import sys
from datetime import datetime, timezone
from pathlib import Path

from _common import REPO_ROOT

DOCS = REPO_ROOT / "docs"
PLANS = DOCS / "ai/plans"

BOARD_LINE = re.compile(r"^- \[([ x-])\] ([A-Z][0-9][UXTHPS][1-9]) (.*)$")
TASK_HEADING = re.compile(r"^### ([A-Z][0-9][UXTHPS][1-9]) (.*)$")
HEADER_LINE = re.compile(r"^([A-Z][a-z]+): (.*)$")
GROUP = re.compile(r"^[a-z][a-z0-9:-]*$")
KIND = re.compile(r"^[UXTHPS]$")
LAYERS = ("presentation", "domain", "data", "di")
BLOCK_HEADING = re.compile(r"^## (v\d+\.\d+\.\d+) · releases? ([A-Z](?: and [A-Z])*) · (\d{4}-\d{2}-\d{2})$")


class BoardError(Exception):
    pass


def read(path: Path) -> str:
    if not path.is_file():
        raise BoardError(f"{path.relative_to(REPO_ROOT)} is missing")
    return path.read_text()


def sections(text: str, level: str = "## ") -> dict[str, str]:
    """Split a markdown body by headings of one level: heading text -> body."""
    out: dict[str, str] = {}
    current = ""
    body: list[str] = []
    for line in text.splitlines():
        if line.startswith(level) and not line.startswith(level + "#"):
            out[current] = "\n".join(body)
            current, body = line[len(level):].strip(), []
        else:
            body.append(line)
    out[current] = "\n".join(body)
    return out


def joined_items(body: str) -> list[str]:
    """`- ` lines with their two-space continuation lines folded in."""
    items: list[str] = []
    for line in body.splitlines():
        if line.startswith("- "):
            items.append(line[2:].strip())
        elif line.startswith("  ") and items:
            items[-1] += " " + line.strip()
    return items


def parse_board_line(line: str) -> dict | None:
    match = BOARD_LINE.match(line)
    if not match:
        return None
    state, task_id, rest = match.groups()
    parts = [p.strip() for p in rest.split(" · ")]
    task = {
        "id": task_id,
        "state": {" ": "open", "x": "done", "-": "dropped"}[state],
        "title": parts[0],
        "est": None,
        "act": None,
        "decides": [],
        "after": [],
        "blocked": None,
        "dropped": None,
    }
    for part in parts[1:]:
        if re.fullmatch(r"\d+", part):
            task["est"] = int(part)
        elif m := re.fullmatch(r"(\d+) → (\d+)", part):
            task["est"], task["act"] = int(m.group(1)), int(m.group(2))
        elif part.startswith("decides "):
            task["decides"] = re.findall(r"D\d+", part)
        elif part.startswith("after "):
            task["after"] = re.findall(r"[A-Z][0-9][UXTHPS][1-9]", part)
        elif part.startswith("blocked: "):
            task["blocked"] = part[len("blocked: "):]
        elif part.startswith("dropped: "):
            task["dropped"] = part[len("dropped: "):]
        else:
            raise BoardError(f"{task_id}: cannot read `{part}` on its board line")
    if task["est"] is None:
        raise BoardError(f"{task_id}: board line has no points")
    return task


def board_lines(body: str) -> list[dict]:
    return [t for t in (parse_board_line(l) for l in body.splitlines()) if t]


def header(text: str, path: Path) -> dict[str, str]:
    """The `Key: value` lines between the title and the first blank line after them."""
    fields: dict[str, str] = {}
    for line in text.splitlines()[1:]:
        if line.strip() == "" and fields:
            break
        if m := HEADER_LINE.match(line):
            fields[m.group(1).lower()] = m.group(2).strip()
    if "status" not in fields:
        raise BoardError(f"{path.relative_to(REPO_ROOT)}: no `Status:` line")
    return fields


def brief_fields(body: str) -> dict[str, str]:
    """`**Why** …`, `**Done when** …` and the rest of a task section, each as one paragraph."""
    fields: dict[str, str] = {}
    key = None
    for line in body.splitlines():
        m = re.match(r"^\*\*([A-Z][a-z ]+)\*\*\s*(.*)$", line)
        if m:
            key = m.group(1).lower().replace(" ", "_")
            text = m.group(2)
            # `**Checks** … **Depends** …` share a line.
            if "**Depends**" in text:
                text, depends = text.split("**Depends**", 1)
                fields["depends"] = depends.strip()
            fields[key] = text.strip()
        elif key and line.strip():
            fields[key] += " " + line.strip()
    return fields


def parse_sprint(path: Path) -> dict:
    text = read(path)
    fields = header(text, path)
    title = text.splitlines()[0]
    m = re.match(r"^# Sprint ([A-Z][0-9]) · (.*)$", title)
    if not m:
        raise BoardError(f"{path.relative_to(REPO_ROOT)}: title is not `# Sprint <id> · <name>`")
    when = fields.get("when", "")
    start, _, end = when.partition(" → ")
    secs = sections(text)
    tasks: dict[str, dict] = {}
    for heading, body in sections(secs.get("Tasks", ""), "### ").items():
        hm = TASK_HEADING.match("### " + heading) if heading else None
        if not hm:
            continue
        task_id = hm.group(1)
        brief = brief_fields(body)
        tasks[task_id] = {
            "id": task_id,
            "why": brief.get("why", ""),
            "decideFirst": brief.get("decide_first", ""),
            "doneWhen": brief.get("done_when", ""),
            "touches": brief.get("touches", ""),
            "checks": brief.get("checks", ""),
            "depends": brief.get("depends", ""),
        }
    return {
        "id": m.group(1),
        "name": m.group(2).strip(),
        "file": str(path.relative_to(REPO_ROOT)),
        "status": fields["status"],
        "start": start.strip(),
        "end": end.strip(),
        "goal": fields.get("goal", ""),
        "release": fields.get("release", m.group(1)[0]),
        "agents": fields.get("agents", ""),
        "board": board_lines(secs.get("Board", "")),
        "retrospective": secs.get("Retrospective", "").strip(),
        "briefs": tasks,
    }


def parse_release(path: Path) -> dict:
    text = read(path)
    fields = header(text, path)
    title = text.splitlines()[0]
    m = re.match(r"^# Release ([A-Z]) · (.*)$", title)
    if not m:
        raise BoardError(f"{path.relative_to(REPO_ROOT)}: title is not `# Release <letter> · <name>`")
    goal = ""
    for para in re.split(r"\n\s*\n", text):
        if para.startswith("#") or HEADER_LINE.match(para.splitlines()[0]) or para.startswith("Edits after"):
            continue
        goal = " ".join(l.strip() for l in para.splitlines())
        break
    return {
        "letter": m.group(1),
        "name": m.group(2).strip(),
        "file": str(path.relative_to(REPO_ROOT)),
        "status": fields["status"],
        "sprints": [s for s in re.findall(r"[A-Z][0-9]", fields.get("sprints", "")) if s[0] == m.group(1)],
        "goal": goal,
    }


def parse_backlog_item(item: str) -> dict:
    """`<title> · <pts> · <group> [· <kind>] · <why>` (D69). `pts` is a band or `?`, which is
    `None`; the group is a module path at any depth or a process area, and `area`, `feature` and
    `layer` are read off it rather than kept in a list of their own."""
    parts = [p.strip() for p in item.split(" · ")]
    title, rest = parts[0], parts[1:]
    pts = None
    group = None
    kind = None
    if rest and (re.fullmatch(r"\d+", rest[0]) or rest[0] == "?"):
        token = rest.pop(0)
        pts = None if token == "?" else int(token)
    if rest and GROUP.match(rest[0]):
        group = rest.pop(0)
    if rest and KIND.match(rest[0]):
        kind = rest.pop(0)
    was = re.search(r"\(was ([A-Z][0-9][UXTHPS][1-9])\)", title)
    path = group.split(":") if group else []
    return {
        "title": title,
        "pts": pts,
        "group": group,
        "kind": kind,
        "area": path[0] if path else None,
        "feature": path[1] if len(path) > 1 and path[0] == "feature" else None,
        "layer": path[-1] if len(path) > 1 and path[-1] in LAYERS else None,
        "why": " · ".join(rest),
        "was": was.group(1) if was else None,
    }


def parse_backlog() -> dict:
    secs = sections(read(DOCS / "BACKLOG.md"))
    keys = {"Next": "next", "DevOps": "devops", "Someday": "someday", "Behind a decision": "decision"}
    out = {}
    for heading, key in keys.items():
        items = [parse_backlog_item(i) for i in joined_items(secs.get(heading, ""))]
        for item in items:
            if item["group"] is None:
                raise BoardError(f"BACKLOG.md § {heading}: `{item['title'][:50]}` has no group")
        out[key] = items
    return out


def parse_changelog() -> list[dict]:
    blocks = []
    for heading, body in sections(read(DOCS / "CHANGELOG.md")).items():
        m = BLOCK_HEADING.match("## " + heading)
        if not m:
            continue
        lines = [l[2:].strip() for l in body.splitlines() if l.startswith("- ")]
        tasks = re.search(r"^Tasks: (.*)$", body, re.MULTILINE)
        est = re.search(r"^Estimate (\d+) · Actual (\d+) · Ratio ([\d.]+)", body, re.MULTILINE)
        blocks.append({
            "version": m.group(1),
            "releases": m.group(2).split(" and "),
            "date": m.group(3),
            "lines": lines,
            "tasks": tasks.group(1) if tasks else "",
            "estimate": int(est.group(1)) if est else None,
            "actual": int(est.group(2)) if est else None,
            "ratio": float(est.group(3)) if est else None,
        })
    return blocks


def git(*args: str) -> str:
    try:
        return subprocess.run(["git", *args], cwd=REPO_ROOT, capture_output=True, text=True, check=True).stdout.strip()
    except (subprocess.CalledProcessError, FileNotFoundError):
        return ""


def build() -> dict:
    status_text = read(DOCS / "STATUS.md")
    status = sections(status_text)
    releases = {r["letter"]: r for r in (parse_release(p) for p in sorted(PLANS.glob("?.md")))}
    sprints = {s["id"]: s for s in (parse_sprint(p) for p in sorted(PLANS.glob("??-*.md")))}

    # Board lines by id, from the one board: STATUS.md's § Board, its § Release sections, and every
    # draft's own § Board.
    lines: dict[str, dict] = {}
    release_sections: dict[str, list[dict]] = {}
    for heading, body in status.items():
        for task in board_lines(body):
            lines[task["id"]] = task
        if m := re.match(r"^Release ([A-Z])", heading):
            release_sections[m.group(1)] = board_lines(body)
    for sprint in sprints.values():
        for task in sprint["board"]:
            lines.setdefault(task["id"], task)

    def tasks_of(sprint: dict) -> list[dict]:
        ids = [t["id"] for t in sprint["board"]] or [
            i for i in lines if i[:2] == sprint["id"]
        ]
        ordered = sorted(set(ids), key=lambda i: (list(lines).index(i) if i in lines else 99))
        out = []
        for task_id in ordered:
            line = lines.get(task_id) or {"id": task_id, "state": "open", "title": "", "est": None, "act": None,
                                          "decides": [], "after": [], "blocked": None, "dropped": None}
            out.append({**line, **sprint["briefs"].get(task_id, {})})
        return out

    for sprint in sprints.values():
        sprint["tasks"] = tasks_of(sprint)
        sprint["points"] = sum(t["est"] or 0 for t in sprint["tasks"])
        sprint["actual"] = sum(t["act"] or 0 for t in sprint["tasks"] if t["act"] is not None)
        del sprint["board"], sprint["briefs"]

    open_sprints = [s for s in sprints.values() if s["status"] == "open"]
    if len(open_sprints) > 1:
        raise BoardError("more than one sprint is `Status: open`: " + ", ".join(s["id"] for s in open_sprints))
    drafts = [s for s in sprints.values() if s["status"] == "draft"]
    # Queue order is STATUS.md § Drafts; a draft it does not list goes last.
    drafts_text = status.get("Drafts", "")
    drafts.sort(key=lambda s: (drafts_text.find(s["id"]) if s["id"] in drafts_text else 10**6))

    for release in releases.values():
        release["sprintsDone"] = [s for s in sprints.values() if s["release"] == release["letter"] and s["status"].startswith("done")]
        release["open"] = release["status"] == "open"
    open_releases = [r for r in releases.values() if r["open"]]
    if len(open_releases) > 1:
        raise BoardError("more than one release is `Status: open`")

    headline = status_text.split("\n\n", 1)[1].split("\n\n", 1)[0].replace("\n", " ") if "\n\n" in status_text else ""

    return {
        "syncedAt": datetime.now(timezone.utc).isoformat(timespec="seconds"),
        "commit": git("rev-parse", "--short", "HEAD"),
        "branch": git("rev-parse", "--abbrev-ref", "HEAD"),
        "dirty": bool(git("status", "--porcelain")),
        "headline": headline,
        "sprint": open_sprints[0] if open_sprints else None,
        "drafts": drafts,
        "release": open_releases[0] if open_releases else None,
        "releases": sorted(releases.values(), key=lambda r: r["letter"]),
        "backlog": parse_backlog(),
        "shipped": parse_changelog(),
    }


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("--dry-run", action="store_true", help="parse everything and report counts; print no JSON")
    args = parser.parse_args()
    try:
        board = build()
    except BoardError as error:
        print(f"board.py: {error}", file=sys.stderr)
        return 1
    if args.dry_run:
        sprint = board["sprint"]
        print(f"sprint: {sprint['id'] + ' · ' + sprint['name'] if sprint else 'none open'}")
        print(f"drafts: {len(board['drafts'])} · releases: {len(board['releases'])} · shipped: {len(board['shipped'])}")
        print("backlog: " + " · ".join(f"{k} {len(v)}" for k, v in board["backlog"].items()))
        return 0
    json.dump(board, sys.stdout, indent=1, ensure_ascii=False)
    print()
    return 0


if __name__ == "__main__":
    sys.exit(main())
