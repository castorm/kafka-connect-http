package com.github.castorm.kafka.connect.http.response.timestamp;

/*-
 * #%L
 * Kafka Connect HTTP Plugin
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
