package com.github.castorm.kafka.connect.http.response.timestamp;

/*-
 * #%L
 * Kafka Connect HTTP Plugin
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

import java.time.ZoneId;
import java.util.Map;

import static com.github.castorm.kafka.connect.http.response.timestamp.NattyTimestampParserConfigTest.Fixture.config;
import static com.github.castorm.kafka.connect.http.response.timestamp.NattyTimestampParserConfigTest.Fixture.zoneId;
import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;

class NattyTimestampParserConfigTest {

    @Test
    void whenItemsParserClassConfigured_thenInitialized() {
        assertThat(config(ImmutableMap.of("http.response.record.timestamp.parser.zone", zoneId)).getTimestampZoneId()).contains(ZoneId.of(zoneId));
    }

    @Test
    void whenMissingItemParserClassConfigured_thenDefault() {
        assertThat(config(emptyMap()).getTimestampZoneId()).isEmpty();
    }

    interface Fixture {
        String zoneId = "Europe/Paris";

        static NattyTimestampParserConfig config(Map<String, String> settings) {
            return new NattyTimestampParserConfig(settings);
        }
    }
}
