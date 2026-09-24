#!/usr/bin/env python3
"""Validate LogicMonitor dashboard JSON and optional DataSource exports."""

from __future__ import annotations

import argparse
import json
import sys
from pathlib import Path
from typing import TYPE_CHECKING

sys.path.insert(0, str(Path(__file__).resolve().parent))

if TYPE_CHECKING:
    from jsonschema import Draft202012Validator

from lib import (
    SCHEMA_DIR,
    check_alert_widget_rules,
    check_widget_datasource_rules,
    extract_metric_references,
    extract_noc_display_names,
    load_datapoint_index_from_exports,
    load_json,
    resolve_datasource,
)


def load_validator() -> "Draft202012Validator":
    from jsonschema import Draft202012Validator
    from jsonschema.validators import RefResolver

    schema_path = SCHEMA_DIR / "dashboard.schema.json"
    common_path = SCHEMA_DIR / "common.defs.json"
    schema = load_json(schema_path)
    common = load_json(common_path)
    store = {
        schema_path.as_uri(): schema,
        common_path.as_uri(): common,
        "common.defs.json": common,
    }
    resolver = RefResolver(base_uri=schema_path.as_uri(), referrer=schema, store=store)
    return Draft202012Validator(schema, resolver=resolver)


def validate_schema(dashboard: dict) -> list[str]:
    try:
        validator = load_validator()
    except ImportError:
        return [
            "JSON Schema validation requires the optional jsonschema package "
            "(pip install jsonschema). Default semantic checks do not."
        ]
    except Exception as exc:
        return [f"Invalid bundled schema: {exc}"]

    errors: list[str] = []
    for error in sorted(validator.iter_errors(dashboard), key=lambda e: list(e.path)):
        path = ".".join(str(part) for part in error.path) or "(root)"
        errors.append(f"[schema] {path}: {error.message}")
    return errors


def validate_datapoints(dashboard: dict, index: dict) -> list[str]:
    errors: list[str] = []
    lookup = index.get("lookup", index)

    for widget in dashboard.get("widgets", []):
        if not isinstance(widget, dict):
            continue
        cfg = widget.get("config", {})

        for rule_error in check_widget_datasource_rules(cfg):
            errors.append(rule_error)

        for ref in extract_metric_references(cfg):
            full_name = ref.get("dataSourceFullName", "")
            dp_name = ref.get("dataPointName", "")
            if not full_name or not dp_name or dp_name == "*":
                continue
            if ref.get("note") == "pieChartItem slice":
                continue

            entry = resolve_datasource(lookup, full_name)
            if not entry:
                errors.append(
                    f"[{ref['widgetType']}] unknown dataSourceFullName: {full_name}"
                )
                continue
            if dp_name not in entry.get("dataPoints", []):
                errors.append(
                    f"[{ref['widgetType']}] datapoint '{dp_name}' not in datasource "
                    f"'{entry.get('displayName')} ({entry.get('name')})'"
                )

        for display_name in extract_noc_display_names(cfg):
            if display_name == "__FORBIDDEN:dataSourceFullName__":
                errors.append("noc items must not include dataSourceFullName")

    return errors


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("dashboard", help="Dashboard JSON file to validate")
    parser.add_argument(
        "--datapoints",
        nargs="+",
        help="DataSource export files or directories for datapoint cross-check",
    )
    parser.add_argument(
        "--schema-only",
        action="store_true",
        help="Validate against bundled JSON Schema only (implies --with-schema)",
    )
    parser.add_argument(
        "--with-schema",
        action="store_true",
        help="Also validate against bundled JSON Schema (optional jsonschema package)",
    )
    args = parser.parse_args()

    dashboard_path = Path(args.dashboard)
    try:
        dashboard = load_json(dashboard_path)
    except (json.JSONDecodeError, OSError) as exc:
        print(f"Failed to read dashboard: {exc}", file=sys.stderr)
        return 1

    errors: list[str] = []
    if args.with_schema or args.schema_only:
        errors.extend(validate_schema(dashboard))
    if not args.schema_only:
        for widget in dashboard.get("widgets", []):
            cfg = widget.get("config", {})
            errors.extend(check_widget_datasource_rules(cfg))
            errors.extend(check_alert_widget_rules(cfg))

    if args.datapoints:
        index = {"lookup": load_datapoint_index_from_exports([Path(p) for p in args.datapoints])}
        if not index["lookup"]:
            errors.append(
                "No usable DataSource JSON found: expected non-empty dataPoints "
                "(portal export) or datapoints (LogicModule import bundle)."
            )
        else:
            errors.extend(validate_datapoints(dashboard, index))

    if errors:
        print(f"Validation failed for {dashboard_path}:")
        for error in errors[:20]:
            print(f"  - {error}")
        if len(errors) > 20:
            print(f"  ... and {len(errors) - 20} more")
        return 1

    print(f"OK: {dashboard_path}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
