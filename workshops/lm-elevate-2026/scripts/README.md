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

The packer also enforces the session progression: Sessions 01–04 remain graph-free, Session 03B and later node checkpoints carry the `auto.status=online` Active Discovery filter, and Session 05 receives the final standard and overview graph definitions.
