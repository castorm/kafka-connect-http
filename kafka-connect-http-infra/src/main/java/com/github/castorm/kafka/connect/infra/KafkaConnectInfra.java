package com.github.castorm.kafka.connect.infra;

/*-
 * #%L
 * kafka-connect-http
 * %%
 * Copyright (C) 2020 CastorM
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
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.Network;

import java.time.Duration;
import java.util.stream.Stream;

import static org.testcontainers.containers.BindMode.READ_ONLY;
import static org.testcontainers.containers.Network.newNetwork;
import static org.testcontainers.utility.DockerImageName.parse;

@Getter
public class KafkaConnectInfra {

    private final Network network;

    private final KafkaContainer kafka;

    private final KafkaConnectContainer kafkaConnect;

    public KafkaConnectInfra() {
        network = newNetwork();

//        Use following container instead for ARM Architecture (i.e. M2 Apple chip like)
//        kafka = new KafkaContainer(parse("confluentinc/cp-kafka:7.5.3.arm64"))
        kafka = new KafkaContainer(parse("confluentinc/cp-kafka:7.5.3"))
            .withNetwork(network)
                .withNetworkAliases("kafka");

//        Use following container instead for ARM Architecture (i.e. M2 Apple chip like)
//        kafkaConnect = new KafkaConnectContainer(parse("confluentinc/cp-kafka-connect:7.5.3.arm64"))
        kafkaConnect = new KafkaConnectContainer(parse("confluentinc/cp-kafka-connect:7.5.3"))
            .withNetwork(network)
                .withBootstrapServers("PLAINTEXT://kafka:9092")
                .withFileSystemBind("kafka-connect-http/target", "/etc/kafka-connect/plugins/kafka-connect-http", READ_ONLY)
                .withStartupTimeout(Duration.ofMinutes(3));
    }

    public KafkaConnectInfra start() {
        kafka.start();
        kafkaConnect.start();
        kafkaConnect.waitingUntilReady();
        System.out.println("Kafka Connect cluster is ready" +
                "\n  REST API: http://" + getKafkaConnectExternalRestUrl() +
                "\n  Debug Agent: " + getKafkaConnectExternalDebugUrl());
        return this;
    }

    public void stop() {
        Stream.of(kafka, kafkaConnect).parallel().forEach(GenericContainer::stop);
    }

    public String getKafkaConnectExternalRestUrl() {
        return kafkaConnect.getHost() + ":" + kafkaConnect.getMappedPort(kafkaConnect.getRestPort());
    }

    public String getKafkaConnectExternalDebugUrl() {
        return kafkaConnect.getHost() + ":" + kafkaConnect.getMappedPort(kafkaConnect.getDebugPort());
    }

    public String getKafkaBootstrapServers() {
        return kafka.getBootstrapServers();
    }
}
