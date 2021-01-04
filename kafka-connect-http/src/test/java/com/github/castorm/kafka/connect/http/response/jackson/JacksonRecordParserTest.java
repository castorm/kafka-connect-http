package com.github.castorm.kafka.connect.http.response.jackson;

/*-
 * #%L
 * kafka-connect-http
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

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.fasterxml.jackson.core.JsonPointer.compile;
import static com.github.castorm.kafka.connect.http.response.jackson.JacksonRecordParserTest.Fixture.deserialize;
import static com.github.castorm.kafka.connect.http.response.jackson.JacksonRecordParserTest.Fixture.item1;
import static com.github.castorm.kafka.connect.http.response.jackson.JacksonRecordParserTest.Fixture.jsonK1K2;
import static com.github.castorm.kafka.connect.http.response.jackson.JacksonRecordParserTest.Fixture.pointerToK1;
import static com.github.castorm.kafka.connect.http.response.jackson.JacksonRecordParserTest.Fixture.pointerToK2;
import static com.github.castorm.kafka.connect.http.response.jackson.JacksonRecordParserTest.Fixture.v1;
import static com.github.castorm.kafka.connect.http.response.jackson.JacksonRecordParserTest.Fixture.v2;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static java.util.Optional.empty;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class JacksonRecordParserTest {

    JacksonRecordParser parser = new JacksonRecordParser();

    @Mock
    JacksonRecordParserConfig config;

    @BeforeEach
    void setUp() {
        parser = new JacksonRecordParser(__ -> config, new JacksonSerializer());
    }

    @Test
    void givenNoPointer_whenGetKey_thenEmpty() {

        given(config.getKeyPointer()).willReturn(emptyList());
        parser.configure(emptyMap());

        assertThat(parser.getKey(jsonK1K2)).isEmpty();
    }

    @Test
    void givenPointer_whenGetKey_thenKeyAsText() {

        given(config.getKeyPointer()).willReturn(singletonList(pointerToK1));
        parser.configure(emptyMap());

        assertThat(parser.getKey(jsonK1K2)).contains(v1);
    }

    @Test
    void givenPointers_whenGetKey_thenKeyAsTextConcatenated() {

        given(config.getKeyPointer()).willReturn(asList(pointerToK1, pointerToK2));
        parser.configure(emptyMap());

        assertThat(parser.getKey(jsonK1K2)).contains(v1 + "+" + v2);
    }

    @Test
    void givenPointer_whenGetValueText_thenValue() {

        given(config.getValuePointer()).willReturn(pointerToK1);
        parser.configure(emptyMap());

        assertThat(parser.getValue(jsonK1K2)).isEqualTo(v1);
    }

    @Test
    void givenPointer_whenGetValueObject_thenValue() {

        given(config.getValuePointer()).willReturn(pointerToK1);
        parser.configure(emptyMap());

        assertThat(parser.getValue(jsonK1K2)).isEqualTo(v1);
    }

    @Test
    void givenNoPointer_whenGetTimestamp_thenEmpty() {

        given(config.getTimestampPointer()).willReturn(empty());
        parser.configure(emptyMap());

        assertThat(parser.getTimestamp(jsonK1K2)).isEmpty();
    }

    @Test
    void givenPointer_whenGetTimestamp_thenTimestampAsText() {

        given(config.getTimestampPointer()).willReturn(Optional.of(pointerToK1));
        parser.configure(emptyMap());

        assertThat(parser.getTimestamp(jsonK1K2)).contains(v1);
    }

    @Test
    void givenPointer_whenGetOffset_thenOffset() {

        given(config.getOffsetPointers()).willReturn(ImmutableMap.of("key", pointerToK1));
        parser.configure(emptyMap());

        assertThat(parser.getOffset(jsonK1K2)).isEqualTo(ImmutableMap.of("key", v1));
    }

    interface Fixture {
        ObjectMapper mapper = new ObjectMapper();
        String k1 = "k1";
        String v1 = "v1";
        String k2 = "k2";
        String v2 = "v2";
        String item1 = "{\"" + k1 + "\":\"" + v1 + "\",\"" + k2 + "\":\"" + v2 + "\"}";
        JsonNode jsonK1K2 = deserialize(item1);
        JsonPointer pointerToK1 = compile("/" + k1);
        JsonPointer pointerToK2 = compile("/" + k2);

        @SneakyThrows
        static JsonNode deserialize(String body) {
            return mapper.readTree(body);
        }
    }
}
