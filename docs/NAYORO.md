# Nayoro recording format

OpenIntave uses the event model and serializers supplied by `ac.intave:samples`.
It does not maintain a second local event registry or payload codec.

## Structure

Runtime recordings are Zstandard-compressed UTF-8 JSON Lines. After decompression,
each line has a stable event type and the event data:

```json
{"type":"header","data":{"id":"...","licenseName":"unknown","classifier":"UNKNOWN","createdAt":1786320000000,"offset":0}}
{"type":"combat.attack","data":{"source":42,"target":91,"offset":50}}
```

The first record is an `ac.intave.samples.event.HeaderEvent`. Event `offset`
values are milliseconds since the preceding event. The stream ends at EOF; it
does not have a separate footer record.

`ac.intave.samples.serial.JsonWriter` creates the Zstandard stream and writes
events. `ac.intave.samples.serial.JsonReader` decompresses it and resolves event
types through `ac.intave.samples.event.EventRegistry`.

## Adding an event

Event types and their serialized fields belong in the `ac.intave:samples`
library. Publish a new library version containing the event and its registry
entry, update the dependency in `build.gradle.kts`, then emit the dependency
event through `Nayoro.emit(user, event)`.

OpenIntave-specific conversion code should stay at the integration boundary in
`SampleTypes`; the serialized model must continue to use `ac.intave.samples`
shared values.
