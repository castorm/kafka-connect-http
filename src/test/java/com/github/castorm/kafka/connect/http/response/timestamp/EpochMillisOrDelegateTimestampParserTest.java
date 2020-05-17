package com.github.castorm.kafka.connect.http.response.timestamp;

/*-
 * #%L
 * Kafka Connect HTTP
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

import com.github.castorm.kafka.connect.http.response.timestamp.spi.TimestampParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static java.time.Instant.ofEpochMilli;
import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class EpochMillisOrDelegateTimestampParserTest {

    EpochMillisOrDelegateTimestampParser parser;

    @Mock
    TimestampParser delegate;

    @Mock
    EpochMillisOrDelegateTimestampParserConfig config;

    @BeforeEach
    void setUp() {
        parser = new EpochMillisOrDelegateTimestampParser(__ -> config);
        given(config.getDelegateParser()).willReturn(delegate);
        parser.configure(emptyMap());
    }

    @Test
    void givenNumber_whenParse_thenParsed() {
        assertThat(parser.parse("123")).isEqualTo(ofEpochMilli(123));
    }

    @Test
    void givenNotNumber_whenParse_thenDelegated() {

        parser.parse("abc");

        then(delegate).should().parse("abc");
    }

    @Test
    void givenNotNumber_whenParse_thenReturnedFromDelegate() {

        given(delegate.parse("abc")).willReturn(ofEpochMilli(123));

        assertThat(parser.parse("abc")).isEqualTo(ofEpochMilli(123));
    }
}
