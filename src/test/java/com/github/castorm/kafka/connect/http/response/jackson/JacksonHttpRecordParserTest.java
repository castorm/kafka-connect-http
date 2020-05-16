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
import static com.github.castorm.kafka.connect.http.response.jackson.JacksonHttpRecordParserTest.Fixture.deserialize;
import static com.github.castorm.kafka.connect.http.response.jackson.JacksonHttpRecordParserTest.Fixture.item1;
import static com.github.castorm.kafka.connect.http.response.jackson.JacksonHttpRecordParserTest.Fixture.item2;
import static com.github.castorm.kafka.connect.http.response.jackson.JacksonHttpRecordParserTest.Fixture.itemArray;
import static com.github.castorm.kafka.connect.http.response.jackson.JacksonHttpRecordParserTest.Fixture.itemNested;
import static com.github.castorm.kafka.connect.http.response.jackson.JacksonHttpRecordParserTest.Fixture.mapper;
import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class JacksonHttpRecordParserTest {

    JacksonHttpRecordParser parser = new JacksonHttpRecordParser();

    @Mock
    JacksonHttpRecordParserConfig config;

    @BeforeEach
    void setUp() {
        parser = new JacksonHttpRecordParser(__ -> config, () -> mapper);
    }

    @Test
    void givenPointer_whenGetItemsRoot_thenRoot() {

        given(config.getRecordsPointer()).willReturn(compile("/"));
        parser.configure(emptyMap());

        assertThat(parser.getRecords("{}".getBytes()))
                .containsExactly(deserialize("{}"));
    }

    @Test
    void givenPointer_whenGetItemsArray_thenAllReturned() {

        given(config.getRecordsPointer()).willReturn(compile("/items"));
        parser.configure(emptyMap());

        assertThat(parser.getRecords(itemArray.getBytes()))
                .containsExactly(deserialize(item1), deserialize(item2));
    }

    @Test
    void givenPointer_whenGetItemsNested_thenSingleReturned() {

        given(config.getRecordsPointer()).willReturn(compile("/items"));
        parser.configure(emptyMap());

        assertThat(parser.getRecords(itemNested.getBytes()))
                .containsExactly(deserialize(item1));
    }

    @Test
    void givenPointer_whenGetMissingItemsProperty_thenException() {

        given(config.getRecordsPointer()).willReturn(compile("/missing"));
        parser.configure(emptyMap());

        assertThat(catchThrowable(() -> parser.getRecords(itemArray.getBytes())))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void givenNoPointer_whenGetKey_thenEmpty() {

        given(config.getKeyPointer()).willReturn(Optional.empty());
        parser.configure(emptyMap());

        assertThat(parser.getKey(deserialize("{\"k1\":\"v1\"}"))).isEmpty();
    }

    @Test
    void givenPointer_whenGetKey_thenKey() {

        given(config.getKeyPointer()).willReturn(Optional.of(compile("/k1")));
        parser.configure(emptyMap());

        assertThat(parser.getKey(deserialize("{\"k1\":\"v1\"}")))
                .contains("v1");
    }

    @Test
    void givenPointer_whenGetMissingKey_thenException() {

        given(config.getKeyPointer()).willReturn(Optional.of(compile("/key")));
        parser.configure(emptyMap());

        assertThat(catchThrowable(() -> parser.getKey(deserialize("{\"k1\":\"v1\"}"))))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void givenPointer_whenGetValueText_thenValue() {

        given(config.getValuePointer()).willReturn(compile("/k1"));
        parser.configure(emptyMap());

        assertThat(parser.getValue(deserialize("{\"k1\":\"v1\"}"))).isEqualTo("v1");
    }

    @Test
    void givenPointer_whenGetValueObject_thenValue() {

        given(config.getValuePointer()).willReturn(compile("/k1"));
        parser.configure(emptyMap());

        assertThat(parser.getValue(deserialize("{\"k1\": {\"k2\":\"v1\"}}"))).isEqualTo("{\"k2\":\"v1\"}");
    }

    @Test
    void givenPointer_whenGetMissingValue_thenException() {

        given(config.getValuePointer()).willReturn(compile("/value"));
        parser.configure(emptyMap());

        assertThat(catchThrowable(() -> parser.getValue(deserialize("{\"k1\":\"v1\"}"))))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void givenPointer_whenGetTimestamp_thenTimestamp() {

        given(config.getTimestampPointer()).willReturn(Optional.of(compile("/k1")));
        parser.configure(emptyMap());

        assertThat(parser.getTimestamp(deserialize("{\"k1\": \"timestamp\"}"))).contains("timestamp");
    }

    @Test
    void givenNoPointer_whenGetTimestamp_thenEmty() {

        given(config.getTimestampPointer()).willReturn(Optional.empty());
        parser.configure(emptyMap());

        assertThat(parser.getTimestamp(deserialize("{\"k1\": \"timestamp\"}"))).isEmpty();
    }

    @Test
    void givenPointer_whenGetMissingTimestamp_thenException() {

        given(config.getTimestampPointer()).willReturn(Optional.of(compile("/timestamp")));
        parser.configure(emptyMap());

        assertThat(catchThrowable(() -> parser.getTimestamp(deserialize("{\"k1\":\"v1\"}"))))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void givenPointer_whenGetOffset_thenOffset() {

        given(config.getOffsetPointers()).willReturn(ImmutableMap.of("key", compile("/k1")));
        parser.configure(emptyMap());

        assertThat(parser.getOffsets(deserialize("{\"k1\":\"v1\"}")))
                .isEqualTo(ImmutableMap.of("key", "v1"));
    }

    @Test
    void givenPointer_whenGetOffsetMissing_thenException() {

        given(config.getOffsetPointers()).willReturn(ImmutableMap.of("key", compile("/missing")));
        parser.configure(emptyMap());

        assertThat(catchThrowable(() -> parser.getOffsets(deserialize("{\"k1\":\"v1\"}"))))
                .isInstanceOf(IllegalArgumentException.class);
    }

    interface Fixture {
        ObjectMapper mapper = new ObjectMapper();
        String item1 = "{\"k1\":\"v1\"}";
        String item2 = "{\"k2\":\"v2\"}";
        String itemArray = "{\"items\":[" + item1 + "," + item2 + "]}";
        String itemNested = "{\"items\":" + item1 + "}";

        @SneakyThrows
        static JsonNode deserialize(String body) {
            return mapper.readTree(body);
        }
    }
}
