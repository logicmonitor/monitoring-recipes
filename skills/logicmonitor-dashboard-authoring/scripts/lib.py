"""Shared utilities for dashboard schema tooling (portable skill bundle)."""

from __future__ import annotations

import json
import re
from pathlib import Path
from typing import Any, Iterator


def resolve_schema_dir() -> Path:
    """Resolve <skill>/schema/ next to this scripts/ directory."""
    script_dir = Path(__file__).resolve().parent
    schema_dir = script_dir.parent / "schema"
    if (schema_dir / "dashboard.schema.json").is_file():
        return schema_dir
    raise FileNotFoundError(
        f"Could not find dashboard.schema.json in {schema_dir}"
    )


SCHEMA_DIR = resolve_schema_dir()
SKIP_PARTS = {"Archive", "Packages", "exchange"}


def load_json(path: Path) -> dict:
    return json.loads(path.read_text(encoding="utf-8"))


def build_datasource_aliases(name: str, display_name: str) -> list[str]:
    aliases = {name, display_name}
    if display_name and name:
        aliases.add(f"{display_name} ({name})")
    if display_name:
        aliases.add(f"{display_name}-")
    return sorted(aliases)


def parse_display_name_paren(full_name: str) -> tuple[str | None, str | None]:
    match = re.match(r"^(.+?)\s+\(([^)]+)\)$", full_name)
    if not match:
        return None, None
    return match.group(1).strip(), match.group(2).strip()


def index_datasource_export(data: dict) -> dict[str, Any]:
    name = data.get("name") or data.get("displayName") or ""
    display_name = data.get("displayName") or name
    data_points = []
    for dp in data.get("dataPoints", []):
        if isinstance(dp, dict) and dp.get("name"):
            data_points.append(dp["name"])
    aliases = build_datasource_aliases(name, display_name)
    entry = {
        "name": name,
        "displayName": display_name,
        "dataPoints": sorted(set(data_points)),
        "aliases": aliases,
    }
    keyed: dict[str, Any] = {}
    for alias in aliases:
        keyed[alias] = entry
    return keyed


def load_datapoint_index_from_exports(paths: list[Path]) -> dict[str, Any]:
    index: dict[str, Any] = {}
    for path in paths:
        if path.is_dir():
            files = sorted(path.glob("*.json"))
        else:
            files = [path]
        for file_path in files:
            try:
                data = load_json(file_path)
            except (json.JSONDecodeError, OSError):
                continue
            if "dataPoints" not in data:
                continue
            keyed = index_datasource_export(data)
            for alias, entry in keyed.items():
                index[alias] = entry
    return index


def resolve_datasource(index: dict[str, Any], full_name: str) -> dict[str, Any] | None:
    if full_name in index:
        return index[full_name]
    display, name = parse_display_name_paren(full_name)
    if display and name:
        for key in (f"{display} ({name})", display, name):
            if key in index:
                return index[key]
    if full_name.rstrip("-") in index:
        return index[full_name.rstrip("-")]
    return None


def iter_widgets(dashboard: dict) -> Iterator[dict]:
    for widget in dashboard.get("widgets", []):
        if isinstance(widget, dict) and isinstance(widget.get("config"), dict):
            yield widget["config"]


def extract_metric_references(config: dict) -> list[dict[str, str]]:
    refs: list[dict[str, str]] = []
    widget_type = config.get("type", "")

    if widget_type == "cgraph":
        for dp in config.get("graphInfo", {}).get("dataPoints", []):
            refs.append(
                {
                    "widgetType": widget_type,
                    "dataSourceFullName": dp.get("dataSourceFullName", ""),
                    "dataPointName": dp.get("dataPointName", ""),
                }
            )
    elif widget_type == "bigNumber":
        for dp in config.get("bigNumberInfo", {}).get("dataPoints", []):
            refs.append(
                {
                    "widgetType": widget_type,
                    "dataSourceFullName": dp.get("dataSourceFullName", ""),
                    "dataPointName": dp.get("dataPointName", ""),
                }
            )
    elif widget_type == "pieChart":
        info = config.get("pieChartInfo", {})
        for dp in info.get("dataPoints", []):
            refs.append(
                {
                    "widgetType": widget_type,
                    "dataSourceFullName": dp.get("dataSourceFullName", ""),
                    "dataPointName": dp.get("dataPointName", ""),
                }
            )
        for item in info.get("pieChartItems", []):
            refs.append(
                {
                    "widgetType": widget_type,
                    "dataSourceFullName": "",
                    "dataPointName": item.get("dataPointName", ""),
                    "note": "pieChartItem slice",
                }
            )
    elif widget_type == "gauge":
        dp = config.get("dataPoint", {})
        refs.append(
            {
                "widgetType": widget_type,
                "dataSourceFullName": dp.get("dataSourceFullName", ""),
                "dataPointName": dp.get("dataPointName", ""),
            }
        )
    elif widget_type == "dynamicTable":
        full_name = config.get("dataSourceFullName", "")
        for col in config.get("columns", []):
            refs.append(
                {
                    "widgetType": widget_type,
                    "dataSourceFullName": full_name,
                    "dataPointName": col.get("dataPointName", ""),
                }
            )
    elif widget_type == "deviceSLA":
        for metric in config.get("metrics", []):
            refs.append(
                {
                    "widgetType": widget_type,
                    "dataSourceFullName": metric.get("dataSourceFullName", ""),
                    "dataPointName": metric.get("metric", ""),
                }
            )
    return refs


def extract_noc_display_names(config: dict) -> list[str]:
    if config.get("type") != "noc":
        return []
    names: list[str] = []
    for item in config.get("items", []):
        value = item.get("dataSourceDisplayName")
        if isinstance(value, str):
            names.append(value)
        if item.get("dataSourceFullName"):
            names.append("__FORBIDDEN:dataSourceFullName__")
    return names


def validate_noc_display_name_pattern(value: str) -> bool:
    if not value:
        return False
    if value == "*":
        return True
    if re.search(r"[()*|]", value):
        return True
    if value.endswith("-"):
        return True
    return bool(value.strip())


def check_alert_widget_rules(config: dict) -> list[str]:
    errors: list[str] = []
    if config.get("type") != "alert":
        return errors

    display = config.get("displaySettings", {})
    sort_value = display.get("sort")
    if sort_value is not None and not isinstance(sort_value, str):
        errors.append(
            "alert displaySettings.sort must be a string such as '-startEpoch', not an object"
        )

    play_sound = display.get("playSound")
    if play_sound is not None and not isinstance(play_sound, dict):
        errors.append("alert displaySettings.playSound must be an object, not a boolean")

    filters = config.get("filters", {})
    if filters.get("group", "").startswith("##"):
        errors.append(
            "alert filters.group must be URL-encoded (e.g. %23%23defaultResourceGroup%23%23*), not raw ##tokens##"
        )

    return errors


def check_widget_datasource_rules(config: dict) -> list[str]:
    errors: list[str] = []
    widget_type = config.get("type")

    if widget_type == "noc":
        for item in config.get("items", []):
            if "dataSourceFullName" in item:
                errors.append("noc items must use dataSourceDisplayName, not dataSourceFullName")
            display = item.get("dataSourceDisplayName")
            if isinstance(display, str) and not validate_noc_display_name_pattern(display):
                errors.append(f"invalid noc dataSourceDisplayName pattern: {display}")
    elif widget_type == "dynamicTable":
        for row in config.get("rows", []):
            if "deviceGroupFullPath" in row:
                errors.append("dynamicTable rows must use groupFullPath, not deviceGroupFullPath")
    elif widget_type == "cgraph":
        for dp in config.get("graphInfo", {}).get("dataPoints", []):
            for field in ("instanceName", "deviceDisplayName", "deviceGroupFullPath"):
                val = dp.get(field)
                if isinstance(val, str):
                    errors.append(
                        f"cgraph dataPoints[].{field} must be a glob object, not a plain string"
                    )
    return errors
