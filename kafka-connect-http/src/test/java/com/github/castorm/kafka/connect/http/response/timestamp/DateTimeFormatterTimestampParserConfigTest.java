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
import java.time.format.DateTimeFormatter;
import java.util.Map;

import static com.github.castorm.kafka.connect.http.response.timestamp.DateTimeFormatterTimestampParserConfigTest.Fixture.config;
import static com.github.castorm.kafka.connect.http.response.timestamp.DateTimeFormatterTimestampParserConfigTest.Fixture.date;
import static com.github.castorm.kafka.connect.http.response.timestamp.DateTimeFormatterTimestampParserConfigTest.Fixture.isoDate;
import static java.time.format.DateTimeFormatter.ofPattern;
import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;

class DateTimeFormatterTimestampParserConfigTest {

    @Test
    void whenItemTimestampParserPatternConfigured_thenInitialized() {
        assertThat(config(ImmutableMap.of("http.response.record.timestamp.parser.pattern", "yyyy-MM-dd")).getRecordTimestampFormatter().parse(date).toString())
                .isEqualTo(ofPattern("yyyy-MM-dd").withZone(ZoneId.of("UTC")).parse(date).toString());
    }

    @Test
    void whenItemTimestampParserZoneConfigured_thenInitialized() {
        assertThat(config(ImmutableMap.of("http.response.record.timestamp.parser.zone", "America/New_York")).getRecordTimestampFormatter().parse(isoDate).toString())
                .isEqualTo(ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX").withZone(ZoneId.of("America/New_York")).parse(isoDate).toString());
    }

    @Test
    void whenMissingItemTimestampParserClassConfigured_thenInitialized() {
        assertThat(config(emptyMap()).getRecordTimestampFormatter()).isInstanceOf(DateTimeFormatter.class);
    }

    interface Fixture {
        String date = "2020-01-01";
        String isoDate = "2020-01-01T01:23:45.000Z";

        static DateTimeFormatterTimestampParserConfig config(Map<String, String> settings) {
            return new DateTimeFormatterTimestampParserConfig(settings);
        }
    }
}
