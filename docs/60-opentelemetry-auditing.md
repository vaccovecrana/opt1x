# OpenTelemetry auditing

When enabled, Opt1x will send data CRUD (Create, Read, Update, Delete) events to an [OTEL compatible collector](https://opentelemetry.io/docs/languages/sdk-configuration/otlp-exporter/).

## Logging Namespace

The logging namespace for OTEL audit events is `opt1x.*`.

This prefix groups all audit events under the Opt1x domain. Each event includes a key field `opt1x.event` that specifies the action (e.g., `createApiKey`, `updateConfig`), paired with relevant context such as `opt1x.kidname` (API key name), `opt1x.nsIdpath` (namespace path), and other field-specific identifiers like `opt1x.valname` (value name) or `opt1x.cidname` (configuration name).

This structured logging facilitates easy filtering and correlation in OTEL collectors.

## Common Useful Search Queries

Retrieve all creation events (e.g., `createApiKey`, `createNamespace`) to monitor new resource setups:
```
opt1x.event=create*
```

Filter all actions performed by a specific API key (e.g., `o1x` or `linda-key`) for auditing key usage.
```
opt1x.kidname=<key-name> AND opt1x.event=*
```

Identify all changes within a namespace subtree (e.g., `/o1x/trek`) to track configuration or value updates.
```
opt1x.nsIdpath=/o1x/* AND opt1x.event=update* OR opt1x.event=create* OR opt1x.event=delete*
```

Find unauthorized access attempts by API keys, useful for security reviews.
```
opt1x.event=noNsAccess
```

Retrieve detailed load events including remote host and protocol data for a specific key, aiding in debugging access patterns.
```
opt1x.event=loadConfig AND opt1x.kidname=<key-name>
```

## Enabling OTEL Auditing

To enable OTEL auditing, configure the OTEL exporter in your Opt1x setup by specifying the collector endpoint via command line options.

## Event Details and Examples

Events are logged using SLF4J with key-value pairs. For example:
```
{opt1x.event=createNamespace} {opt1x.kidname=o1x} {opt1x.nsIdpath=/o1x/trek}
```
