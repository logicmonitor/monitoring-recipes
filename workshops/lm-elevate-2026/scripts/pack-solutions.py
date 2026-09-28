#!/usr/bin/env python3
"""Pack workshop scripts into scaffold and solution JSON files."""

from __future__ import annotations

import argparse
import json
import sys
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]


ARTIFACTS = (
    {
        "json": "sessions/01-propertysource/module-scaffold.json",
        "fields": {"script.content": "sessions/01-propertysource/scripts/student.groovy"},
    },
    {
        "json": "sessions/02-controller-single/module-scaffold.json",
        "fields": {"collectionAttrs.content": "sessions/02-controller-single/scripts/student.groovy"},
    },
    {
        "json": "sessions/03a-active-discovery/module-scaffold.json",
        "fields": {"activeDiscovery.params.content": "sessions/03a-active-discovery/scripts/student.groovy"},
    },
    {
        "json": "sessions/03b-node-collection/module-scaffold.json",
        "fields": {
            "collectionAttrs.content": "sessions/03b-node-collection/scripts/student.groovy",
            "activeDiscovery.params.content": "sessions/03a-active-discovery/scripts/reference.groovy",
        },
    },
    {
        "json": "sessions/04-refactor/propertysource-scaffold.json",
        "fields": {"script.content": "sessions/04-refactor/scripts/student-propertysource.groovy"},
    },
    {
        "json": "sessions/04-refactor/controller-scaffold.json",
        "fields": {"collectionAttrs.content": "sessions/04-refactor/scripts/student-controller.groovy"},
    },
    {
        "json": "sessions/04-refactor/node-scaffold.json",
        "fields": {
            "collectionAttrs.content": "sessions/04-refactor/scripts/student-node-collect.groovy",
            "activeDiscovery.params.content": "sessions/04-refactor/scripts/student-node-ad.groovy",
        },
    },
    {
        "json": "solutions/01-propertysource/addCategory_Training_Fabric.json",
        "fields": {"script.content": "sessions/01-propertysource/scripts/reference.groovy"},
    },
    {
        "json": "solutions/02-controller-single/Training_Fabric_Controller.json",
        "fields": {"collectionAttrs.content": "sessions/02-controller-single/scripts/reference.groovy"},
    },
    {
        "json": "solutions/03-node-multi/Training_Fabric_Node.json",
        "fields": {
            "collectionAttrs.content": "sessions/03b-node-collection/scripts/reference.groovy",
            "activeDiscovery.params.content": "sessions/03a-active-discovery/scripts/reference.groovy",
        },
    },
    {
        "json": "solutions/04-refactored/addCategory_Training_Fabric.json",
        "fields": {"script.content": "sessions/04-refactor/scripts/reference-propertysource.groovy"},
    },
    {
        "json": "solutions/04-refactored/Training_Fabric_Controller.json",
        "fields": {"collectionAttrs.content": "sessions/04-refactor/scripts/reference-controller.groovy"},
    },
    {
        "json": "solutions/04-refactored/Training_Fabric_Node.json",
        "fields": {
            "collectionAttrs.content": "sessions/04-refactor/scripts/reference-node-collect.groovy",
            "activeDiscovery.params.content": "sessions/04-refactor/scripts/reference-node-ad.groovy",
        },
    },
    {
        "json": "sessions/05-import-and-validate/addCategory_Training_Fabric.json",
        "fields": {"script.content": "sessions/04-refactor/scripts/reference-propertysource.groovy"},
    },
    {
        "json": "sessions/05-import-and-validate/Training_Fabric_Controller.json",
        "fields": {"collectionAttrs.content": "sessions/04-refactor/scripts/reference-controller.groovy"},
    },
    {
        "json": "sessions/05-import-and-validate/Training_Fabric_Node.json",
        "fields": {
            "collectionAttrs.content": "sessions/04-refactor/scripts/reference-node-collect.groovy",
            "activeDiscovery.params.content": "sessions/04-refactor/scripts/reference-node-ad.groovy",
        },
    },
)


def set_field(document: dict, path: str, value: str) -> None:
    parts = path.split(".")
    target = document
    for part in parts[:-1]:
        target = target[part]
    target[parts[-1]] = value


def expected_document(item: dict) -> tuple[Path, dict]:
    json_path = ROOT / item["json"]
    document = json.loads(json_path.read_text(encoding="utf-8"))
    for field, source in item["fields"].items():
        set_field(document, field, (ROOT / source).read_text(encoding="utf-8"))
    return json_path, document


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--check", action="store_true", help="report drift without writing JSON files")
    args = parser.parse_args()

    drift = False
    for item in ARTIFACTS:
        json_path, expected = expected_document(item)
        current = json.loads(json_path.read_text(encoding="utf-8"))
        if current != expected:
            drift = True
            print(f"DRIFT: {json_path.relative_to(ROOT)}")
            if not args.check:
                json_path.write_text(json.dumps(expected, indent=2) + "\n", encoding="utf-8")
                print(f"Packed: {json_path.relative_to(ROOT)}")

    if drift and args.check:
        return 1
    if not drift:
        print("OK: workshop scripts match scaffold and solution JSON files")
    return 0


if __name__ == "__main__":
    sys.exit(main())
