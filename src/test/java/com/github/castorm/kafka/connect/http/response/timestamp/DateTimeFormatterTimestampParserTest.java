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
