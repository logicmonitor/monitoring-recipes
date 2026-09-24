#!/usr/bin/env python3
"""Manual regression check for extract_collection_keys (stdlib only)."""

from __future__ import annotations

import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))

from lib import extract_collection_keys, greenfield_validate, namevalue_script_output_key


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
    assert (
        namevalue_script_output_key("##WILDVALUE##.example_metric", True, True)
        == "example_metric"
    )
    assert namevalue_script_output_key("example_metric", False, False) == "example_metric"
    assert greenfield_validate({"displayedAs": "Module-Name"})
    assert not greenfield_validate({"displayedAs": "Module Name"})
    assert not greenfield_validate({"displayedAs": "Module Name-"})
    print("OK: extract_collection_keys")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
