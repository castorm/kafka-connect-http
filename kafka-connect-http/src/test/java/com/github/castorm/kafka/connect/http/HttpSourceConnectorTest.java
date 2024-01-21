package com.github.castorm.kafka.connect.http;

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

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

class HttpSourceConnectorTest {

    HttpSourceConnector connector = new HttpSourceConnector();

    @Test
    void whenTaskClass_thenHttpSourceTask() {
        assertThat(connector.taskClass()).isEqualTo(HttpSourceTask.class);
    }

    @Test
    void whenConfig_thenNotEmpty() {
        assertThat(connector.config().configKeys()).isNotEmpty();
    }

    @Test
    void whenSeveralTaskConfigs_thenAsManyReturned() {
        ImmutableMap<String, String> myMap = ImmutableMap.of("endpoint.include.list", "e1,e2,e3,e4");
        connector.start(myMap);
        assertThat(connector.taskConfigs(3)).hasSize(3);
    }

    @Test
    void whenSeveralTaskConfigs_thenAllWithConnectorConfig() {

        ImmutableMap<String, String> myMap = ImmutableMap.of("key", "value", "endpoint.include.list", "e1,e2,e3,e4");
        ImmutableMap<String, String> m1 = ImmutableMap.of("key", "value", "endpoint.include.list", "e1,e4");
        ImmutableMap<String, String> m2 = ImmutableMap.of("key", "value", "endpoint.include.list", "e2");
        ImmutableMap<String, String> m3 = ImmutableMap.of("key", "value", "endpoint.include.list", "e3");

        connector.start(myMap);

        assertThat(connector.taskConfigs(3)).containsExactly(m1, m2, m3);
    }

    @Test
    void whenStop_thenSettingsNull() {

        connector.stop();

        assertThat(connector.taskConfigs(1)).isEqualTo(singletonList(null));
    }

    @Test
    void whenGetVersion_thenNotEmpty() {
        assertThat(connector.version()).isNotEmpty();
    }
}
