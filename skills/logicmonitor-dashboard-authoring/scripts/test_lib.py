"""Regression checks for dashboard DataSource input normalization."""

from __future__ import annotations

import unittest

from lib import index_datasource_export, load_datapoint_index_from_exports


class DatapointIndexTests(unittest.TestCase):
    def test_indexes_portal_export_shape(self) -> None:
        index = index_datasource_export(
            {
                "name": "Example_Source",
                "displayName": "Example Source",
                "dataPoints": [{"name": "metric"}],
            }
        )

        self.assertEqual(index["Example Source (Example_Source)"]["dataPoints"], ["metric"])

    def test_indexes_import_bundle_shape(self) -> None:
        index = index_datasource_export(
            {
                "name": "Example_Source",
                "displayedAs": "Example Source",
                "datapoints": [{"name": "metric"}],
            }
        )

        self.assertEqual(index["Example Source (Example_Source)"]["dataPoints"], ["metric"])

    def test_ignores_json_without_datapoints(self) -> None:
        self.assertEqual(load_datapoint_index_from_exports([]), {})


if __name__ == "__main__":
    unittest.main()
