# Training API contract

The workshop API is a small authenticated REST service hosted by the instructor.

```text
https://lm-elevate-api-indol.vercel.app/api/v1
```

## Authentication

Every request uses the workshop credentials:

| Property | Example | Purpose |
|---|---|---|
| `system.hostname` | `lm-elevate-api-indol.vercel.app` | API host; scripts prepend `https://` and append `/api/v1` |
| `fabric.api.user` | `SEE_INSTRUCTOR_NOTES` | Basic Auth username |
| `fabric.api.pass` | `SEE_INSTRUCTOR_NOTES` | Basic Auth password |

`GET /auth/token` accepts Basic Auth and returns a short-lived bearer token. All other endpoints accept that bearer token. The scripts construct the base URL as `https://${system.hostname}/api/v1`.

```json
{
  "access_token": "training-token-abc123",
  "expires_in": 3600,
  "token_type": "Bearer"
}
```

## Endpoints

### `GET /auth/token`

Authenticates the request and returns a token. The workshop first calls this endpoint every run, then caches the token during the refactor stage.

### `GET /controller`

Returns controller-level health for the single-instance DataSource.

```json
{
  "name": "Enterprise Fabric Controller",
  "site": "training-east",
  "version": "7.4.2",
  "health": 1,
  "node_count": 3,
  "api_latency_ms": 42
}
```

### `GET /nodes`

Returns discoverable fabric nodes.

```json
[
  {"id":"fabric-node-01","name":"Fabric Node 01","role":"leaf","site":"training-east"},
  {"id":"fabric-node-02","name":"Fabric Node 02","role":"spine","site":"training-east"},
  {"id":"fabric-node-03","name":"Fabric Node 03","role":"border","site":"training-west"}
]
```

### `GET /nodes/{id}`

Returns metrics for one discovered node.

```json
{
  "id": "fabric-node-01",
  "health": 1,
  "cpu_percent": 34.5,
  "memory_percent": 61.2,
  "interface_count": 48,
  "error_rate_percent": 0.2
}
```

## Failure responses

- `401` — invalid or missing credentials/token.
- `404` — unknown node ID.
- `429` — too many requests; scripts should fail clearly rather than emit bad metrics.
- `500` — simulated controller failure used for troubleshooting discussion.

The response fields and IDs are stable across the workshop. Controller latency and node CPU, memory, and error-rate values vary per request around a higher 9:00–17:00 America/New_York business-hour baseline so graphs look like live data. Health and topology remain stable.
