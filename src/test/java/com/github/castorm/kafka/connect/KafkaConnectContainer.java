package com.github.castorm.kafka.connect;

/*-
 * #%L
 * Kafka Connect HTTP
 * %%
 * Copyright (C) 2020 Cástor Rodríguez
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import lombok.Getter;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

public class KafkaConnectContainer extends GenericContainer<KafkaConnectContainer> {

    @Getter
    private final Integer restPort = 8083;

    private final Integer jmxPort = 5555;

    public KafkaConnectContainer(DockerImageName dockerImageName) {
        super(dockerImageName);
        withEnv("KAFKA_JMX_PORT", jmxPort.toString());
        withEnv("CONNECT_REST_PORT", restPort.toString());
        withEnv("CONNECT_GROUP_ID", "test");
        withEnv("CONNECT_CONFIG_STORAGE_TOPIC", "test-config");
        withEnv("CONNECT_OFFSET_STORAGE_TOPIC", "test-offsets");
        withEnv("CONNECT_STATUS_STORAGE_TOPIC", "test-status");
        withEnv("CONNECT_CONFIG_STORAGE_REPLICATION_FACTOR", "1");
        withEnv("CONNECT_OFFSET_STORAGE_REPLICATION_FACTOR", "1");
        withEnv("CONNECT_STATUS_STORAGE_REPLICATION_FACTOR", "1");
        withEnv("CONNECT_REST_ADVERTISED_HOST_NAME", "test");
        withEnv("CONNECT_LOG4J_ROOT_LOGLEVEL", "WARN");
        withEnv("CONNECT_PLUGIN_PATH", "/usr/share/java,/etc/kafka-connect/plugins");
        withEnv("CONNECT_KEY_CONVERTER", "org.apache.kafka.connect.storage.StringConverter");
        withEnv("CONNECT_VALUE_CONVERTER", "org.apache.kafka.connect.storage.StringConverter");
        withEnv("CONNECT_INTERNAL_KEY_CONVERTER", "org.apache.kafka.connect.json.JsonConverter");
        withEnv("CONNECT_INTERNAL_VALUE_CONVERTER", "org.apache.kafka.connect.json.JsonConverter");
        withExposedPorts(restPort, jmxPort);
    }

    public KafkaConnectContainer withBootstrapServers(String bootstrapServers) {
        withEnv("CONNECT_BOOTSTRAP_SERVERS", bootstrapServers);
        return this;
    }

    public void waitingUntilReady() {
        Wait.forHttp("/connectors").forPort(restPort).forStatusCode(200).waitUntilReady(this);
    }
}
