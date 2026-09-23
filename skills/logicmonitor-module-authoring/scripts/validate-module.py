#!/usr/bin/env python3
"""Validate LogicMonitor module bundle or import JSON (stdlib; optional JSON Schema)."""

from __future__ import annotations

import argparse
import json
import sys
from pathlib import Path
from typing import TYPE_CHECKING

sys.path.insert(0, str(Path(__file__).resolve().parent))

from lib import (
    SCHEMA_DIR,
    load_json,
    resolve_bundle,
    schema_for_module,
    semantic_validate,
)

if TYPE_CHECKING:
    from jsonschema import Draft202012Validator


def _load_jsonschema_validator(schema_path: Path) -> "Draft202012Validator":
    from jsonschema import Draft202012Validator
    from jsonschema.validators import RefResolver

    schema = load_json(schema_path)
    common_path = SCHEMA_DIR / "logicmodule.common.defs.json"
    store = {
        schema_path.as_uri(): schema,
        common_path.as_uri(): load_json(common_path),
        "logicmodule.common.defs.json": load_json(common_path),
    }
    for extra in SCHEMA_DIR.glob("*.schema.json"):
        store[extra.as_uri()] = load_json(extra)
        store[extra.name] = load_json(extra)
    resolver = RefResolver(base_uri=schema_path.as_uri(), referrer=schema, store=store)
    return Draft202012Validator(schema, resolver=resolver)


def validate_schema(module: dict) -> list[str]:
    try:
        from jsonschema import Draft202012Validator  # noqa: F401
    except ImportError:
        return [
            "JSON Schema validation requires the optional jsonschema package "
            "(pip install jsonschema). Semantic checks do not."
        ]

    try:
        schema_path = schema_for_module(module)
    except ValueError as exc:
        return [str(exc)]

    validator = _load_jsonschema_validator(schema_path)
    errors: list[str] = []
    for err in sorted(validator.iter_errors(module), key=lambda e: e.path):
        loc = ".".join(str(p) for p in err.path) or "(root)"
        errors.append(f"[schema] {loc}: {err.message}")
    return errors


def main() -> int:
    parser = argparse.ArgumentParser(
        description=(
            "Validate LogicModule import JSON or bundle. "
            "Default: semantic checks only (no pip dependencies)."
        )
    )
    parser.add_argument("path", type=Path, help="Bundle directory or .json file")
    parser.add_argument(
        "--with-schema",
        action="store_true",
        help="Also validate against bundled JSON Schema (optional jsonschema package)",
    )
    parser.add_argument(
        "--schema-only",
        action="store_true",
        help="JSON Schema only, skip semantic checks (implies --with-schema)",
    )
    args = parser.parse_args()

    use_schema = args.with_schema or args.schema_only

    try:
        bundle_dir, json_path, module = resolve_bundle(args.path)
    except (OSError, ValueError, json.JSONDecodeError) as exc:
        print(exc, file=sys.stderr)
        return 1

    errors: list[str] = []
    if use_schema:
        errors.extend(validate_schema(module))
    if not args.schema_only:
        errors.extend(semantic_validate(bundle_dir, module))

    if errors:
        for err in errors:
            print(err, file=sys.stderr)
        return 1

    mode = "schema+semantic" if use_schema and not args.schema_only else (
        "schema" if args.schema_only else "semantic"
    )
    print(f"OK ({mode}): {json_path}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
