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

import com.github.castorm.kafka.connect.WiremockContainer;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;

import java.lang.reflect.Method;

import static org.testcontainers.containers.BindMode.READ_ONLY;
import static org.testcontainers.utility.DockerImageName.parse;

public class KafkaConnectInfraExtension implements InvocationInterceptor {

    KafkaConnectInfra infra = new KafkaConnectInfra();

//        Use following container instead for ARM Architecture (i.e. M2 Apple chip like)
//  WiremockContainer wiremock = new WiremockContainer(parse("wiremock/wiremock:nightly"))
    WiremockContainer wiremock = new WiremockContainer(parse("rodolpheche/wiremock:2.25.1"))
            .withNetwork(infra.getNetwork())
            .withNetworkAliases("wiremock")
            .withClasspathResourceMapping("__files", "/home/wiremock/__files", READ_ONLY)
            .withClasspathResourceMapping("mappings", "/home/wiremock/mappings", READ_ONLY);

    public KafkaConnectInfraExtension start() {
        infra.start();
        wiremock.start();
        wiremock.waitingUntilReady();
        return this;
    }

    @Override
    public void interceptAfterAllMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) {
        infra.stop();
        wiremock.stop();
    }

    public String getKafkaConnectExternalUrl() {
        return infra.getKafkaConnectExternalRestUrl();
    }

    public String getKafkaBootstrapServers() {
        return infra.getKafkaBootstrapServers();
    }

    public String getWiremockInternalUrl() {
        return "wiremock:" + wiremock.getPort();
    }
}
