#!/usr/bin/env python3
"""Manual regression check for extract_collection_keys (stdlib only)."""

from __future__ import annotations

import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))

from lib import extract_collection_keys


def main() -> int:
    script = '''
emit.dp("stat_hp", 1)
emit.dp("stat_attack", 2)
'''
    batch = '''
emit.dp(instance, "example_metric", 0)
'''
    keys = extract_collection_keys(script, batchscript=False)
    batch_keys = extract_collection_keys(batch, batchscript=True)
    assert keys == {"stat_hp", "stat_attack"}, keys
    assert batch_keys == {"example_metric"}, batch_keys
    print("OK: extract_collection_keys")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
