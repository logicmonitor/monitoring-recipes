# Module packaging and portal import

Lessons from importing workshop DataSources and PropertySources. Wrong format or
schema produces generic portal errors, not useful Groovy stack traces.

See also: [module-types.md](module-types.md)

## Import format by module type

LogicMonitor's export API is not the same for every LogicModule type:

| Types | Export / file-import format |
|-------|-----------------------------|
| DataSource, EventSource, ConfigSource, LogSource, BatchJob | XML (`format=xml`) |
| PropertySource, TopologySource, DiagnosticSource, RemediationSource | JSON (`format=file`) |

For DataSources, import **XML** through *Modules → My Module Toolbox → Add →
Import from File*. Uploading DataSource JSON there fails with **Internal server
error**. That JSON is the REST API v3 model (`POST /setting/datasources`,
`X-Version: 3`), useful as a source of truth, not as a file-import artifact.

PropertySources have no XML form. Import the JSON export directly.

## DataSource display names

A DataSource `displayName` / `displayedas` may contain `-` **only as the last
character**. `Open-Meteo Current Weather` is rejected with:

```
"-" is only supported for DataSource display name when it is the last char
```

Use spaces (`Open Meteo Current Weather`) or a trailing hyphen style used by
legacy modules (`BGP-`).

This rule is **display name**, not `name` (the internal `Vendor_Product_Thing`
identifier).

## Do not ship portal-computed fields

Strip `id`, `checksum`, and `lineageId` from JSON you intend to import or
re-upload. Those are assigned by the portal. Leaving them in causes import
rejection or 500s.

## Complex datapoints in XML

JSON `postProcessorMethod: "expression"` maps to XML as:

- `postprocessormethod=expression`
- expression text in `postprocessorparam`
- `iscomposite=false`
- `rpn` empty

Putting the expression in `rpn` and setting `iscomposite=true` yields
**XML could not be read into LogicModule**. Copy a real portal XML export, do
not invent the composite/RPN encoding.

Regular collected datapoints use `postprocessormethod=namevalue` (or the
processor you actually selected in the UI).

## Scripts vs embedded copies

Keep standalone `.groovy` as the source of truth. Embed into JSON, then generate
XML from JSON. If those three drift, the portal runs whatever is inside the
imported module, not the file on disk.
