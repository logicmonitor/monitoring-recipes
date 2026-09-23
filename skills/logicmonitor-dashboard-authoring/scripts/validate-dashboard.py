#!/usr/bin/env python3
"""Validate LogicMonitor dashboard JSON against schema and optional DataSource exports."""

from __future__ import annotations

import argparse
import json
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))

try:
    from jsonschema import Draft202012Validator
    from jsonschema.exceptions import SchemaError
    from jsonschema.validators import RefResolver
except ImportError:
    print("Install jsonschema: pip install jsonschema", file=sys.stderr)
    raise SystemExit(1)

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


def load_validator() -> Draft202012Validator:
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
        help="Skip per-widget datasource rule checks",
    )
    args = parser.parse_args()

    dashboard_path = Path(args.dashboard)
    try:
        dashboard = load_json(dashboard_path)
    except (json.JSONDecodeError, OSError) as exc:
        print(f"Failed to read dashboard: {exc}", file=sys.stderr)
        return 1

    try:
        validator = load_validator()
    except SchemaError as exc:
        print(f"Invalid schema: {exc}", file=sys.stderr)
        return 1

    schema_errors = sorted(validator.iter_errors(dashboard), key=lambda e: list(e.path))
    if schema_errors:
        print(f"Schema validation failed for {dashboard_path}:")
        for error in schema_errors[:20]:
            path = ".".join(str(p) for p in error.path) or "(root)"
            print(f"  - {path}: {error.message}")
        if len(schema_errors) > 20:
            print(f"  ... and {len(schema_errors) - 20} more")
        return 1

    rule_errors: list[str] = []
    if not args.schema_only:
        for widget in dashboard.get("widgets", []):
            cfg = widget.get("config", {})
            rule_errors.extend(check_widget_datasource_rules(cfg))
            rule_errors.extend(check_alert_widget_rules(cfg))

    if args.datapoints:
        index = {"lookup": load_datapoint_index_from_exports([Path(p) for p in args.datapoints])}
        rule_errors.extend(validate_datapoints(dashboard, index))

    if rule_errors:
        print(f"Datasource rule validation failed for {dashboard_path}:")
        for error in rule_errors:
            print(f"  - {error}")
        return 1

    print(f"OK: {dashboard_path}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
