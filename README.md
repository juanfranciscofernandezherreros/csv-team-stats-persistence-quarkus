# csv-team-stats-persistence-quarkus
Quarkus JVM / Java 21 migration of the original Spring Kafka worker.

## Run
Requires PostgreSQL, Kafka, Confluent Schema Registry, and access to the private Maven GitHub Packages `basketball-event-contracts:1.6.0`.
```bash
mvn test
mvn package
java -jar target/quarkus-app/quarkus-run.jar
```
Set DB_URL, DB_USER, DB_PASS, KAFKA_BOOTSTRAP_SERVERS and KAFKA_SCHEMA_REGISTRY_URL.

Kafka consumer reads raw bytes, decodes Avro explicitly, processes a poll as a JDBC batch, publishes success/rejected, then manually commits. Failures retry 3x before raw-byte DLT; corrupt Avro records go to DLT. This is at-least-once with possible duplicate output events after retries, not an atomic database/Kafka transaction.

## Memory
Initial Agroal pool: 1–5. Measure RSS under realistic load before setting a container limit or JVM heap.

## Outstanding validation
This draft has not been compiled or integration-tested yet; restore Spring integration/E2E tests for exact parity of DLT, tracing, offset commits and error classifications before production.
