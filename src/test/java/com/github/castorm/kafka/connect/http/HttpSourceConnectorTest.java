package com.github.castorm.kafka.connect.http;

/*-
 * #%L
 * kafka-connect-http-plugin
 * %%
 * Copyright (C) 2020 CastorM
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
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
        assertThat(connector.taskConfigs(3)).hasSize(3);
    }

    @Test
    void whenSeveralTaskConfigs_thenAllWithConnectorConfig() {

        ImmutableMap<String, String> myMap = ImmutableMap.of("key", "value");

        connector.start(myMap);

        assertThat(connector.taskConfigs(3)).containsExactly(myMap, myMap, myMap);
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
