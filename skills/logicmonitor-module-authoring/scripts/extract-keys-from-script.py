#!/usr/bin/env python3
"""Print collection keys heuristically extracted from collect.groovy|ps1."""

from __future__ import annotations

import argparse
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))

from lib import extract_collection_keys, find_script, SCRIPT_COLLECT_NAMES


def main() -> int:
    parser = argparse.ArgumentParser(description="Extract metric keys from collect script")
    parser.add_argument("path", type=Path, help="Module bundle directory")
    parser.add_argument(
        "--batchscript",
        action="store_true",
        help="Parse instance.key=value (batchscript) patterns",
    )
    args = parser.parse_args()

    collect = find_script(args.path, SCRIPT_COLLECT_NAMES)
    if not collect:
        print("No collect.groovy or collect.ps1 found", file=sys.stderr)
        return 1

    text = collect.read_text(encoding="utf-8")
    keys = sorted(extract_collection_keys(text, batchscript=args.batchscript))
    for key in keys:
        print(key)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
