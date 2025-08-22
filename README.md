# JMS Stub (IBM MQ, multi-broker, optional TLS)

Spring Boot app that listens on queues across multiple IBM MQ brokers and replies to configured queues. Supports TLS per-broker using keystore/truststore and optional cipher suite.

Note: Project uses Lombok for boilerplate reduction. Ensure Lombok plugin is enabled in your IDE.

## Configure

Edit `src/main/resources/application.yml`:
- `stub.brokers`: list of IBM MQ endpoints
  - `id`, `host`, `port`, `queueManager`, `channel`, `username`, `password`
  - `tls`: `enabled`, optional `cipherSuite`, `truststorePath/password/type`, `keystorePath/password/type`
- `stub.routes`: each route specifies which broker/queue to listen on and where to send replies
- Optional `staticResponse`. If omitted, app replies with JSON `{status:"ok", echo:"..."}`

Keystore/truststore paths can be absolute or `classpath:` URLs. Cipher suite must be enabled on the channel (e.g. `TLS_AES_128_GCM_SHA256`).

## Build

```bash
cd /workspace
mvn -q -DskipTests package
```

## Run

```bash
java -jar target/jms-stub-0.1.0.jar
```

Override TLS at runtime if needed:

```bash
java -Dstub.brokers[1].tls.truststorePath=/path/truststore.jks \
  -Dstub.brokers[1].tls.truststorePassword=changeit \
  -Dstub.brokers[1].tls.cipherSuite=TLS_AES_128_GCM_SHA256 \
  -jar target/jms-stub-0.1.0.jar
```

## Notes
- Uses IBM MQ Jakarta client: `com.ibm.mq:com.ibm.mq.allclient`.
- JMS API is `jakarta.jms.*` (compatible with Spring Boot 3+).
- Maintains `JMSCorrelationID`; falls back to `JMSMessageID`.