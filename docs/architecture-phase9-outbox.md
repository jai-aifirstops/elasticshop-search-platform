# ElasticShop Transactional Outbox

## Reliable write path

```text
API
 |
 v
PostgreSQL transaction
 |
 +--- products update
 +--- outbox_events insert
 |
 v
COMMIT
 |
 v
Outbox polling publisher
 |
 v
Kafka
 |
 v
Idempotent consumer
 |
 +--- Elasticsearch update
 +--- Redis invalidation
 +--- processed_events record
```

The product change and outbox record commit in the same local database transaction.

If Kafka is temporarily unavailable, the unpublished row remains in PostgreSQL and the relay retries it.

New events use:

`eventId|UPSERT|productId`

The consumer checks `processed_events` and ignores an event UUID that was already handled.

Monitoring endpoint:

`GET /api/outbox/status`

The local portfolio implementation uses a scheduled polling relay. A larger platform could replace the relay with CDC such as Debezium.
