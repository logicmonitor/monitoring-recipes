#!/usr/bin/env python3
"""Build datapoint-index.json from exported LogicMonitor DataSource JSON files."""

from __future__ import annotations

import argparse
import json
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
from lib import index_datasource_export, load_json


def collect_export_files(inputs: list[str]) -> list[Path]:
    paths: list[Path] = []
    for raw in inputs:
        path = Path(raw)
        if path.is_dir():
            paths.extend(sorted(path.glob("*.json")))
        elif path.is_file():
            paths.append(path)
        else:
            paths.extend(sorted(Path().glob(raw)))
    return paths


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument(
        "inputs",
        nargs="+",
        help="DataSource JSON files or directories containing exports",
    )
    parser.add_argument(
        "-o",
        "--output",
        default="datapoint-index.json",
        help="Output index file path (default: datapoint-index.json)",
    )
    args = parser.parse_args()

    files = collect_export_files(args.inputs)
    if not files:
        print("No DataSource JSON files found.", file=sys.stderr)
        return 1

    index: dict[str, dict] = {}
    sources: list[str] = []

    for file_path in files:
        try:
            data = load_json(file_path)
        except (json.JSONDecodeError, OSError) as exc:
            print(f"Skipping {file_path}: {exc}", file=sys.stderr)
            continue
        if "dataPoints" not in data:
            continue
        keyed = index_datasource_export(data)
        canonical_key = f"{data.get('displayName', '')} ({data.get('name', '')})"
        if canonical_key.endswith(" ()"):
            canonical_key = data.get("name", file_path.stem)
        entry = next(iter(keyed.values()))
        index[canonical_key] = entry
        sources.append(str(file_path))

    output = {
        "sourceFiles": sources,
        "datasources": index,
        "lookup": {},
    }
    for entry in index.values():
        for alias in entry["aliases"]:
            output["lookup"][alias] = entry

    out_path = Path(args.output)
    out_path.write_text(json.dumps(output, indent=2) + "\n", encoding="utf-8")
    print(f"Wrote {len(index)} datasources to {out_path}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
