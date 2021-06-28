FROM confluentinc/cp-kafka-connect-base:6.2.0
COPY kafka-connect-http/target/ /opt/connectors/kafka-connect-http/
