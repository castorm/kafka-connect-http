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

import java.time.ZoneId;
import java.util.Optional;

import static java.time.Instant.parse;
import static java.util.Collections.emptyMap;
import static java.util.Optional.empty;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;


@ExtendWith(MockitoExtension.class)
class NattyTimestampParserTest {

    NattyTimestampParser parser;

    @Mock
    NattyTimestampParserConfig config;

    @BeforeEach
    void setUp() {
        parser = new NattyTimestampParser(__ -> config);
    }

    @Test
    void givenDateWithoutTimezone_whenParse_thenParsedWithLocalTimezone() {

        given(config.getTimestampZoneId()).willReturn(empty());
        parser.configure(emptyMap());

        assertThat(parser.parse("2020-04-28T00:19:05"))
                .isEqualTo(parse("2020-04-28T00:19:05Z").atZone(ZoneId.of("UTC")).withZoneSameLocal(ZoneId.systemDefault()).toInstant());
    }

    @Test
    void givenDateWithTimezone_whenParse_thenParsedWithGivenTimezone() {

        given(config.getTimestampZoneId()).willReturn(Optional.of(ZoneId.of("America/New_York")));
        parser.configure(emptyMap());

        assertThat(parser.parse("2020-04-28T00:19:05"))
                .isEqualTo(parse("2020-04-28T04:19:05Z"));
    }
}
