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
        assertThat(config(ImmutableMap.of("http.response.item.timestamp.parser.pattern", "yyyy-MM-dd")).getItemTimestampFormatter().parse(date).toString())
                .isEqualTo(ofPattern("yyyy-MM-dd").withZone(ZoneId.of("UTC")).parse(date).toString());
    }

    @Test
    void whenItemTimestampParserZoneConfigured_thenInitialized() {
        assertThat(config(ImmutableMap.of("http.response.item.timestamp.parser.zone", "America/New_York")).getItemTimestampFormatter().parse(isoDate).toString())
                .isEqualTo(ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX").withZone(ZoneId.of("America/New_York")).parse(isoDate).toString());
    }

    @Test
    void whenMissingItemTimestampParserClassConfigured_thenInitialized() {
        assertThat(config(emptyMap()).getItemTimestampFormatter()).isInstanceOf(DateTimeFormatter.class);
    }

    interface Fixture {
        String date = "2020-01-01";
        String isoDate = "2020-01-01T01:23:45.000Z";

        static DateTimeFormatterTimestampParserConfig config(Map<String, String> settings) {
            return new DateTimeFormatterTimestampParserConfig(settings);
        }
    }
}
