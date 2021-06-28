FROM confluentinc/cp-kafka-connect-base
COPY kafka-connect-http/target/ /opt/connectors/kafka-connect-http/