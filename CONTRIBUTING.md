# Contributing

Thank you for helping improve Monitoring Recipes.

## What belongs here

**In scope:**
- Reusable script building blocks (one protocol pattern per recipe)
- Conceptual documentation and decision guides
- Agent Skill references for LogicModule authoring best practices

**Out of scope:**
- Full vendor-specific module integrations (belong in LM Exchange)
- Duplicate copies of official LogicMonitor product documentation
- Legacy content from the pre-overhaul `master` branch

## Adding a recipe

1. Create a directory under `recipes/groovy/` or `recipes/powershell/` using a pattern-based name (e.g. `snmp-walk`, not `cisco-snmp`)
2. Include these files:
   - `script.groovy` or `script.ps1` — the snippet
   - `recipe.yaml` — metadata (see an existing recipe for the schema)
   - `README.md` — use case, prerequisites, customization points, module-type adaptation table
3. Update `recipes/README.md` index
4. Update `skills/logicmonitor-module-authoring/assets/recipe-index.md`
5. Update progress in `PROJECT.md`

### Recipe checklist

- [ ] Follows [script-structure.md](../skills/logicmonitor-module-authoring/references/script-structure.md)
- [ ] Tested on a LogicMonitor Collector
- [ ] No hardcoded credentials (use device properties / `hostProps`)
- [ ] Placeholders clearly marked (e.g. `INSERT_OID_HERE`)
- [ ] README includes module-type adaptation table
- [ ] `recipe.yaml` lists applicable module types

## Updating the Agent Skill

- Keep `SKILL.md` as a workflow — move detailed content to `references/`
- Keep `SKILL.md` under ~500 lines
- Run `skills-ref validate ./skills/logicmonitor-module-authoring` before submitting
- Update `PROJECT.md` skill reference tracker
- After changing import JSON rules, run template checks: `pack-module.py` and `validate-module.py` on `skills/logicmonitor-module-authoring/assets/module-templates/*`
- With a local `LogicModules/` corpus, refresh bundled schema: `python scripts/logicmodule-schema/extract-schema.py`

## Updating docs

When changing `docs/module-types/` or `docs/concepts/`, check whether the corresponding skill reference file needs the same update.

## Pull requests

- One recipe or one logical change per PR when possible
- Link to relevant official LM support docs
- Update `PROJECT.md` progress checklists
