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
