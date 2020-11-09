package com.github.castorm.kafka.connect;

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

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.Network;

import java.lang.reflect.Method;
import java.util.stream.Stream;

import static org.testcontainers.containers.BindMode.READ_ONLY;
import static org.testcontainers.containers.Network.newNetwork;
import static org.testcontainers.utility.DockerImageName.parse;

public class Infrastructure implements InvocationInterceptor {

    Network network = newNetwork();

    WiremockContainer wiremock = new WiremockContainer(parse("rodolpheche/wiremock:2.25.1"))
            .withNetwork(network)
            .withNetworkAliases("wiremock")
            .withClasspathResourceMapping("__files", "/home/wiremock/__files", READ_ONLY)
            .withClasspathResourceMapping("mappings", "/home/wiremock/mappings", READ_ONLY);

    KafkaContainer kafka = new KafkaContainer(parse("confluentinc/cp-kafka:5.4.3"))
            .withNetwork(network)
            .withNetworkAliases("kafka");

    KafkaConnectContainer kafkaConnect = new KafkaConnectContainer(parse("confluentinc/cp-kafka-connect:5.4.3"))
            .withBootstrapServers("PLAINTEXT://kafka:9092")
            .withNetwork(network)
            .withFileSystemBind("target/kafka-connect-http", "/etc/kafka-connect/plugins/kafka-connect-http", READ_ONLY);

    public Infrastructure start() {
        kafka.start();
        wiremock.start();
        wiremock.waitingUntilReady();
        kafkaConnect.start();
        kafkaConnect.waitingUntilReady();
        return this;
    }

    @Override
    public void interceptAfterAllMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
        Stream.of(wiremock, kafka, kafkaConnect).parallel().forEach(GenericContainer::stop);
    }

    public String getKafkaConnectExternalUrl() {
        return kafkaConnect.getHost() + ":" + kafkaConnect.getMappedPort(kafkaConnect.getRestPort());
    }

    public String getWiremockInternalUrl() {
        return "wiremock:" + wiremock.getPort();
    }

    public String getKafkaBootstrapServers() {
        return kafka.getBootstrapServers();
    }
}
