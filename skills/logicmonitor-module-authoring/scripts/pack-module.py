#!/usr/bin/env python3
"""Pack collect.groovy|ps1 and ad.* script files into module import JSON."""

from __future__ import annotations

import argparse
import json
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))

from lib import check_pack_sync, pack_module, resolve_bundle


def main() -> int:
    parser = argparse.ArgumentParser(description="Pack module bundle scripts into JSON")
    parser.add_argument("path", type=Path, help="Module bundle directory or .json file")
    parser.add_argument(
        "--check",
        action="store_true",
        help="Fail if JSON content does not match script files (no write)",
    )
    args = parser.parse_args()

    try:
        bundle_dir, json_path, module = resolve_bundle(args.path)
    except (OSError, ValueError, json.JSONDecodeError) as exc:
        print(exc, file=sys.stderr)
        return 1

    if args.check:
        errors = check_pack_sync(bundle_dir, module)
        if errors:
            for err in errors:
                print(err, file=sys.stderr)
            return 1
        print(f"OK: {json_path} matches script files")
        return 0

    try:
        changes = pack_module(bundle_dir, module)
    except ValueError as exc:
        print(exc, file=sys.stderr)
        return 1

    if not changes:
        print("No script files found; nothing to pack")
        return 0

    json_path.write_text(json.dumps(module, indent=2) + "\n", encoding="utf-8")
    for line in changes:
        print(f"Packed {line} -> {json_path.name}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
