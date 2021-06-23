FROM confluentinc/cp-kafka-connect-base
ENV CONNECT_PLUGIN_PATH="/usr/share/java,/usr/share/confluent-hub-components,/usr/share/kafka/plugins,/opt/connectors"
COPY kafka-connect-http/target/ /opt/connectors/kafka-connect-http/
RUN confluent-hub install --no-prompt confluentinc/kafka-connect-jdbc:latest
RUN confluent-hub install --no-prompt debezium/debezium-connector-sqlserver:latest
RUN confluent-hub install --no-prompt debezium/debezium-connector-postgresql:latest
RUN confluent-hub install --no-prompt jcustenborder/kafka-connect-transform-common:latest