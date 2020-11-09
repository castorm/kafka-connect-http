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

public class WiremockContainer extends GenericContainer<WiremockContainer> {

    @Getter
    private final Integer port = 8080;

    public WiremockContainer(DockerImageName dockerImageName) {
        super(dockerImageName);
        withExposedPorts(port);
    }

    public void waitingUntilReady() {
        Wait.forHttp("/ping").forPort(port).forStatusCode(200).waitUntilReady(this);
    }
}
