package com.github.castorm.kafka.connect.http.response.jackson;

/*-
 * #%L
 * Kafka Connect HTTP
 * %%
 * Copyright (C) 2020 - 2021 Cástor Rodríguez
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
import java.util.stream.Stream;

import static com.fasterxml.jackson.core.JsonPointer.compile;
import static com.github.castorm.kafka.connect.http.response.jackson.JacksonRecordParserTest.Fixture.deserialize;
import static com.github.castorm.kafka.connect.http.response.jackson.JacksonRecordParserTest.Fixture.item1;
import static com.github.castorm.kafka.connect.http.response.jackson.JacksonResponseRecordParserTest.Fixture.item2;
import static com.github.castorm.kafka.connect.http.response.jackson.JacksonResponseRecordParserTest.Fixture.itemArray;
import static com.github.castorm.kafka.connect.http.response.jackson.JacksonResponseRecordParserTest.Fixture.itemArrayJson;
import static com.github.castorm.kafka.connect.http.response.jackson.JacksonResponseRecordParserTest.Fixture.pointer;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class JacksonResponseRecordParserTest {

    JacksonResponseRecordParser parser;

    @Mock
    JacksonRecordParserConfig config;

    @Mock
    JacksonRecordParser recordParser;

    @Mock
    JacksonSerializer serializer;

    @BeforeEach
    void setUp() {
        parser = new JacksonResponseRecordParser(__ -> config, recordParser, serializer);

        given(recordParser.getKey(deserialize(item1))).willReturn(Optional.of("value"));
    }

    @Test
    void givenRecords_whenGetRecords_thenAllReturned() {

        givenRecords(deserialize(item1), deserialize(item2));

        assertThat(parser.getRecords(itemArray.getBytes()).collect(toList())).hasSize(2);
    }

    @Test
    void givenRecordWithKey_whenGetRecords_thenItemKeyMapped() {

        givenRecords(deserialize(item1));
        given(recordParser.getKey(deserialize(item1))).willReturn(Optional.of("value"));

        assertThat(parser.getRecords(itemArray.getBytes()).findFirst().get().getKey()).isEqualTo("value");
    }

    @Test
    void givenRecordWithoutKey_whenGetRecords_thenItemKeyNull() {

        givenRecords(deserialize(item1));
        given(recordParser.getKey(deserialize(item1))).willReturn(Optional.empty());

        assertThat(parser.getRecords(itemArray.getBytes()).findFirst().get().getKey()).isNull();
    }

    @Test
    void givenRecordWithTimestamp_whenGetRecords_thenItemTimestampMapped() {

        givenRecords(deserialize(item1));
        given(recordParser.getTimestamp(deserialize(item1))).willReturn(Optional.of("value"));

        assertThat(parser.getRecords(itemArray.getBytes()).findFirst().get().getTimestamp()).isEqualTo("value");
    }

    @Test
    void givenRecordWithoutTimestamp_whenGetRecords_thenItemTimestampNull() {

        givenRecords(deserialize(item1));
        given(recordParser.getTimestamp(deserialize(item1))).willReturn(Optional.empty());

        assertThat(parser.getRecords(itemArray.getBytes()).findFirst().get().getTimestamp()).isNull();
    }

    @Test
    void givenRecordWithOffset_whenGetRecords_thenItemOffsetMapped() {

        givenRecords(deserialize(item1));
        given(recordParser.getOffset(deserialize(item1))).willReturn(ImmutableMap.of("k", "v"));

        assertThat(parser.getRecords(itemArray.getBytes()).findFirst().get().getOffset()).isEqualTo(ImmutableMap.of("k", "v"));
    }

    @Test
    void givenRecordWithValue_whenGetRecords_thenItemBodyMapped() {

        givenRecords(deserialize(item1));
        given(recordParser.getValue(deserialize(item1))).willReturn("value");

        assertThat(parser.getRecords(itemArray.getBytes()).findFirst().get().getBody()).isEqualTo("value");
    }

    private void givenRecords(JsonNode... records) {
        given(config.getRecordsPointer()).willReturn(pointer);
        given(serializer.deserialize(itemArray.getBytes())).willReturn(itemArrayJson);
        given(serializer.getArrayAt(itemArrayJson, pointer)).willReturn(Stream.of(records));
        parser.configure(emptyMap());
    }

    interface Fixture {
        ObjectMapper mapper = new ObjectMapper();
        String property = "v1";
        String item1 = "{\"k1\":\"" + property + "\"}";
        String item2 = "{\"k2\":\"v2\"}";
        String itemArray = "{\"items\":[" + item1 + "," + item2 + "]}";
        JsonNode itemArrayJson = deserialize(itemArray);
        JsonPointer pointer = compile("/items");

        @SneakyThrows
        static JsonNode deserialize(String body) {
            return mapper.readTree(body);
        }
    }
}
