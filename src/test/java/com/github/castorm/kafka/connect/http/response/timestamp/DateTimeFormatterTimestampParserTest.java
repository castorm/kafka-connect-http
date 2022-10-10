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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import static com.github.castorm.kafka.connect.http.response.timestamp.DateTimeFormatterTimestampParserTest.Fixture.date;
import static com.github.castorm.kafka.connect.http.response.timestamp.DateTimeFormatterTimestampParserTest.Fixture.formatter;
import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;
import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class DateTimeFormatterTimestampParserTest {

    DateTimeFormatterTimestampParser parser;

    @Mock
    DateTimeFormatterTimestampParserConfig config;

    @BeforeEach
    void setUp() {
        parser = new DateTimeFormatterTimestampParser(__ -> config);
    }

    @Test
    void givenFormatter_whenParse_thenDelegated() {

        given(config.getRecordTimestampFormatter()).willReturn(formatter);
        parser.configure(emptyMap());

        assertThat(parser.parse(date)).isEqualTo(OffsetDateTime.parse(date, formatter).toInstant());
    }

    interface Fixture {
        String date = "2011-12-03T10:15:30+01:00";
        DateTimeFormatter formatter = ISO_DATE_TIME.withZone(ZoneId.of("UTC"));
    }
}
