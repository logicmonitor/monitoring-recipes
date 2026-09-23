# Themes and Graph Colors

Do not invent theme or color values not listed here. JSON Schema in `schema/common.defs.json` enforces the main graph display enums.

## Themes (allowed)

```
borderPurple, borderGray, borderBlue
solidPurple, solidGray, solidBlue
simplePurple, simpleBlue, simpleGray
newBorderGray, newBorderBlue, newBorderDarkBlue
newSolidGray, newSolidBlue, newSolidDarkBlue
newSimpleGray, newSimpleBlue, newSimpleDarkBlue
```

**Default for authoring:** `newSolidDarkBlue`

**Secondary (section headers / graphs):** `newBorderDarkBlue`

## Graph colors (allowed)

Use `Auto` unless copying a known color from a reference dashboard.

Named colors observed in corpus:

```
aqua, black, blue, fuchsia, gray, green, lime, maroon, navy,
olive, orange, orange2, purple, red, red1, red2, silver, teal, yellow
```

Auto variants (equivalent): `Auto`, `auto`, `AUTO`

## Graph line types

Under `graphInfo.dataPoints[].display.type`:

```
line, stack, area, column
```

## Display options

Under `display.option`:

```
custom, none, default
```

## Timescales and intervals

Default widget `timescale`: `day`. Trend graphs often use `2days`. Default `interval`: `3` (minutes).
