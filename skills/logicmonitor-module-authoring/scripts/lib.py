"""Shared utilities for LogicModule bundle pack and validate."""

from __future__ import annotations

import json
import re
from pathlib import Path
from typing import Any

SCRIPT_COLLECT_NAMES = ("collect.groovy", "collect.ps1")
SCRIPT_AD_NAMES = ("ad.groovy", "ad.ps1")

ALLOWED_AD_DISCOVERY_INTERVALS = frozenset({"0m", "15m", "60m", "1440m"})
GREENFIELD_DEFAULT_AD_DISCOVERY_INTERVAL = "60m"

LANG_BY_FILE = {
    ".groovy": "groovy",
    ".ps1": "powershell",
}

MODULE_TYPE_SCHEMA = {
    0: "datasource.schema.json",
    6: "configsource.schema.json",
    1: "eventsource.schema.json",
    5: "script-module.schema.json",
    11: "script-module.schema.json",
    12: "script-module.schema.json",
}


def resolve_schema_dir() -> Path:
    script_dir = Path(__file__).resolve().parent
    schema_dir = script_dir.parent / "schema"
    if (schema_dir / "logicmodule.schema.json").is_file():
        return schema_dir
    raise FileNotFoundError(f"Could not find logicmodule.schema.json in {schema_dir}")


SCHEMA_DIR = resolve_schema_dir()


def load_json(path: Path) -> dict[str, Any]:
    return json.loads(path.read_text(encoding="utf-8"))


def find_script(path: Path, names: tuple[str, ...]) -> Path | None:
    for name in names:
        candidate = path / name
        if candidate.is_file():
            return candidate
    return None


def script_lang(path: Path) -> str:
    ext = path.suffix.lower()
    if ext not in LANG_BY_FILE:
        raise ValueError(f"Unsupported script extension: {path.name}")
    return LANG_BY_FILE[ext]


def resolve_bundle(path: Path) -> tuple[Path, Path, dict[str, Any]]:
    """Return (bundle_dir, json_path, module_dict)."""
    if path.is_file():
        if path.suffix.lower() != ".json":
            raise ValueError(f"Expected .json file or module directory: {path}")
        bundle_dir = path.parent
        module = load_json(path)
        return bundle_dir, path, module

    if not path.is_dir():
        raise FileNotFoundError(path)

    json_files = sorted(path.glob("*.json"))
    if not json_files:
        raise FileNotFoundError(f"No .json in module bundle: {path}")

    preferred = path / f"{path.name}.json"
    json_path = preferred if preferred.is_file() else json_files[0]
    module = load_json(json_path)
    return path, json_path, module


def apply_script_blob(target: dict[str, Any], script_path: Path) -> None:
    target["type"] = script_lang(script_path)
    target["content"] = script_path.read_text(encoding="utf-8")


def pack_module(bundle_dir: Path, module: dict[str, Any]) -> list[str]:
    """Inline collect/ad files into module dict. Returns change descriptions."""
    changes: list[str] = []
    collect = find_script(bundle_dir, SCRIPT_COLLECT_NAMES)
    ad = find_script(bundle_dir, SCRIPT_AD_NAMES)
    module_type = module.get("type")

    if collect:
        if module_type in (5, 11, 12):
            blob = module.setdefault("script", {})
            apply_script_blob(blob, collect)
            changes.append(f"script.content from {collect.name}")
        elif "collectionAttrs" in module or module_type in (0, 1, 6):
            blob = module.setdefault("collectionAttrs", {})
            apply_script_blob(blob, collect)
            changes.append(f"collectionAttrs.content from {collect.name}")
        else:
            raise ValueError(
                "collect.* present but module has no collectionAttrs or script target"
            )

    if ad:
        ad_block = module.get("activeDiscovery")
        if not isinstance(ad_block, dict):
            raise ValueError("ad.* present but module has no activeDiscovery object")
        params = ad_block.setdefault("params", {})
        apply_script_blob(params, ad)
        changes.append(f"activeDiscovery.params.content from {ad.name}")

    return changes


def content_matches_file(blob: dict[str, Any] | None, script_path: Path) -> bool:
    if not blob or not isinstance(blob, dict):
        return False
    expected = script_path.read_text(encoding="utf-8")
    return blob.get("content") == expected


def check_pack_sync(bundle_dir: Path, module: dict[str, Any]) -> list[str]:
    errors: list[str] = []
    collect = find_script(bundle_dir, SCRIPT_COLLECT_NAMES)
    ad = find_script(bundle_dir, SCRIPT_AD_NAMES)
    module_type = module.get("type")

    if collect:
        if module_type in (5, 11, 12):
            if not content_matches_file(module.get("script"), collect):
                errors.append(
                    f"script.content does not match {collect.name}; run pack-module.py"
                )
        else:
            if not content_matches_file(module.get("collectionAttrs"), collect):
                errors.append(
                    f"collectionAttrs.content does not match {collect.name}; run pack-module.py"
                )

    if ad:
        ad_block = module.get("activeDiscovery") or {}
        params = ad_block.get("params")
        if not content_matches_file(params if isinstance(params, dict) else None, ad):
            errors.append(
                f"activeDiscovery.params.content does not match {ad.name}; run pack-module.py"
            )
    return errors


# Avoid regex literals ending with =""" (breaks some parsers); use normal quoted strings.
_EMIT_KEY_IN_QUOTES = r"['\"]([^'\"]+)['\"]"
_METRIC_KEY = r"([A-Za-z0-9_.-]+)"

_PRINTLN_KEY_EQ = r"[\"']" + _METRIC_KEY + r"="

EMIT_DP_SCRIPT = re.compile(
    r"emit\.dp\s*\(\s*[\"']" + _METRIC_KEY + r"[\"']", re.MULTILINE
)
EMIT_DP_BATCH = re.compile(
    r"emit\.dp\s*\([^,]+,\s*[\"']" + _METRIC_KEY + r"[\"']", re.MULTILINE
)

EMIT_KEY_PATTERNS = [
    EMIT_DP_SCRIPT,
    re.compile(r"lm\.emit\s*\(\s*" + _EMIT_KEY_IN_QUOTES),
    re.compile(r"println\s+" + _PRINTLN_KEY_EQ, re.MULTILINE),
    re.compile(
        r"Write-Output\s+" + _METRIC_KEY + r"=",
        re.IGNORECASE | re.MULTILINE,
    ),
]

BATCH_LINE_PATTERN = re.compile(
    r"println\s+[\"']" + _METRIC_KEY + r"\." + _METRIC_KEY + r"=",
    re.MULTILINE,
)


def extract_collection_keys(script_text: str, batchscript: bool = False) -> set[str]:
    keys: set[str] = set()
    if batchscript:
        for match in EMIT_DP_BATCH.finditer(script_text):
            keys.add(match.group(1))
        for match in BATCH_LINE_PATTERN.finditer(script_text):
            keys.add(match.group(2))
        for match in re.finditer(r"\.([A-Za-z0-9_.-]+)=", script_text):
            keys.add(match.group(1))
        return keys
    for pattern in EMIT_KEY_PATTERNS:
        for match in pattern.finditer(script_text):
            key = match.group(1)
            keys.add(key)
    return keys


def greenfield_validate(module: dict[str, Any]) -> list[str]:
    """Checks for repo-authored import JSON (not portal exports)."""
    errors: list[str] = []
    if module.get("registryMetadata") is not None:
        errors.append("greenfield JSON should omit registryMetadata")
    if module.get("integrationMetadata") is not None:
        errors.append("greenfield JSON should omit integrationMetadata")
    if module.get("version") is not None:
        errors.append("greenfield JSON should omit version (portal assigns on save)")

    ad = module.get("activeDiscovery")
    if isinstance(ad, dict):
        interval = str(ad.get("discoveryInterval") or "")
        if interval and interval not in ALLOWED_AD_DISCOVERY_INTERVALS:
            allowed = ", ".join(sorted(ALLOWED_AD_DISCOVERY_INTERVALS))
            errors.append(
                f'activeDiscovery.discoveryInterval must be one of: {allowed} (got "{interval}")'
            )

    for dp in module.get("datapoints") or []:
        if not isinstance(dp, dict):
            continue
        for bound in ("min", "max"):
            if dp.get(bound) == "":
                errors.append(
                    f"datapoint '{dp.get('name')}' has empty string {bound}; omit or use a number"
                )
    return errors


def read_collect_script(bundle_dir: Path) -> tuple[str | None, bool]:
    collect = find_script(bundle_dir, SCRIPT_COLLECT_NAMES)
    if not collect:
        return None, False
    return collect.read_text(encoding="utf-8"), collect.suffix.lower() == ".ps1"


def datapoint_names(module: dict[str, Any]) -> set[str]:
    names: set[str] = set()
    for dp in module.get("datapoints") or []:
        if isinstance(dp, dict) and dp.get("name"):
            names.add(dp["name"])
    return names


def namevalue_datapoints(module: dict[str, Any]) -> dict[str, str]:
    mapping: dict[str, str] = {}
    for dp in module.get("datapoints") or []:
        if not isinstance(dp, dict):
            continue
        if dp.get("interpretMethod") == "namevalue" and dp.get("name"):
            mapping[dp["name"]] = dp.get("interpretExpr") or dp["name"]
    return mapping


WILDVALUE_PREFIX = "##WILDVALUE##."


def namevalue_script_output_key(
    interpret_expr: str, batchscript: bool, multi_instance: bool
) -> str:
    """Metric key as printed after 'wildvalue.' in batchscript stdout."""
    expr = interpret_expr or ""
    if batchscript and multi_instance and expr.startswith(WILDVALUE_PREFIX):
        return expr[len(WILDVALUE_PREFIX) :]
    return expr


def graph_datapoint_refs(module: dict[str, Any]) -> list[str]:
    refs: list[str] = []
    for graph in module.get("graphs") or []:
        if not isinstance(graph, dict):
            continue
        for line in graph.get("lines") or []:
            if isinstance(line, dict) and line.get("datapointName"):
                refs.append(line["datapointName"])
        for gdp in graph.get("datapoints") or []:
            if isinstance(gdp, dict) and gdp.get("datapointName"):
                refs.append(gdp["datapointName"])
    return refs


def semantic_validate(
    bundle_dir: Path, module: dict[str, Any], strict_greenfield: bool = False
) -> list[str]:
    errors: list[str] = []
    if strict_greenfield:
        errors.extend(greenfield_validate(module))
    errors.extend(check_pack_sync(bundle_dir, module))

    if module.get("type") != 0:
        return errors

    dps = datapoint_names(module)
    nv = namevalue_datapoints(module)
    collect_text, _ = read_collect_script(bundle_dir)
    batch = module.get("collectionMethod") == "batchscript"
    multi = bool(module.get("multiInstance"))
    if collect_text and nv:
        emitted = extract_collection_keys(collect_text, batchscript=batch)
        for dp_name, expr in nv.items():
            if batch and multi:
                if not (expr or "").startswith(WILDVALUE_PREFIX):
                    errors.append(
                        f"datapoint '{dp_name}' interpretExpr must be "
                        f"'{WILDVALUE_PREFIX}<name>' for batchscript multi-instance "
                        f"(got '{expr}')"
                    )
                script_key = namevalue_script_output_key(expr, batch, multi)
                if script_key != dp_name:
                    errors.append(
                        f"datapoint '{dp_name}' interpretExpr should be "
                        f"'{WILDVALUE_PREFIX}{dp_name}' "
                        f"(suffix '{script_key}' does not match name)"
                    )
            else:
                script_key = namevalue_script_output_key(expr, batch, multi)
            if (
                script_key not in emitted
                and dp_name not in emitted
                and expr not in emitted
            ):
                errors.append(
                    f"datapoint '{dp_name}' interpretExpr '{expr}' "
                    f"(script key '{script_key}') not found in collect script output keys"
                )

    for ref in graph_datapoint_refs(module):
        if ref not in dps:
            errors.append(f"graph references unknown datapoint '{ref}'")

    if module.get("collectionMethod") == "batchscript" and module.get("multiInstance"):
        if not find_script(bundle_dir, SCRIPT_AD_NAMES):
            errors.append("batchscript multiInstance requires ad.groovy or ad.ps1")
        if not module.get("activeDiscovery"):
            errors.append("batchscript multiInstance requires activeDiscovery in JSON")

    for dp in module.get("datapoints") or []:
        if isinstance(dp, dict) and not dp.get("originId"):
            errors.append(f"datapoint '{dp.get('name')}' missing originId")

    return errors


def schema_for_module(module: dict[str, Any]) -> Path:
    module_type = module.get("type")
    name = MODULE_TYPE_SCHEMA.get(module_type)
    if not name:
        raise ValueError(f"Unsupported module type for schema: {module_type}")
    return SCHEMA_DIR / name
