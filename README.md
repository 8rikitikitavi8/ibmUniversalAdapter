# JMS Stub (Multi-broker, optional TLS)

Small Spring Boot app that listens on queues across multiple JMS brokers (ActiveMQ 5.x), and sends stubbed responses to configured queues. Some brokers can use TLS with keystore/truststore.

## Configure

Edit `src/main/resources/application.yml`:
- Define `stub.brokers`: list of brokers with `id`, `url`, credentials, and `tls` block if needed.
- Define `stub.routes`: each route tells which broker/queue to listen on and where to send replies.
- Optional `staticResponse` per route. If omitted, the app replies with a JSON `{status:"ok", echo:"..."}`.

Note: For TLS, use `ssl://host:port` URL and absolute paths to JKS stores. Classpath paths like `classpath:certs/truststore.jks` are also supported.

## Build

```bash
cd /workspace
mvn -q -DskipTests package
```

## Run

```bash
java -jar target/jms-stub-0.1.0.jar
```

Or with overrides:

```bash
java -Dserver.port=8081 \
  -Dstub.brokers[1].tls.truststorePath=/path/truststore.jks \
  -Dstub.brokers[1].tls.truststorePassword=changeit \
  -jar target/jms-stub-0.1.0.jar
```

## Notes
- Tested with ActiveMQ Classic 5.x. Other JMS brokers may work if they are ActiveMQ-compatible.
- The app preserves/sets `JMSCorrelationID`. If incoming has none, it uses the incoming `JMSMessageID`.