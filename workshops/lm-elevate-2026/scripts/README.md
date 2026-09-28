# Workshop script maintenance

The readable `.groovy` files under the session directories are the source of truth for the workshop scripts. The JSON files contain the same scripts as embedded import content.

From the repository root, run:

```bash
python3 workshops/lm-elevate-2026/scripts/pack-solutions.py --check
```

Use the check before publishing. To update the embedded JSON after editing a script, omit `--check`:

```bash
python3 workshops/lm-elevate-2026/scripts/pack-solutions.py
```

The command updates both student scaffolds and completed solution JSON. Run the module validator after packing.
