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
import java.util.stream.Stream;

import static com.fasterxml.jackson.core.JsonPointer.compile;
import static com.github.castorm.kafka.connect.http.response.jackson.JacksonRecordParserTest.Fixture.deserialize;
import static com.github.castorm.kafka.connect.http.response.jackson.JacksonRecordParserTest.Fixture.item1;
import static com.github.castorm.kafka.connect.http.response.jackson.JacksonRecordParserTest.Fixture.item2;
import static com.github.castorm.kafka.connect.http.response.jackson.JacksonRecordParserTest.Fixture.itemArray;
import static com.github.castorm.kafka.connect.http.response.jackson.JacksonRecordParserTest.Fixture.mapper;
import static com.github.castorm.kafka.connect.http.response.jackson.JacksonRecordParserTest.Fixture.pointer;
import static com.github.castorm.kafka.connect.http.response.jackson.JacksonRecordParserTest.Fixture.property;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class JacksonRecordParserTest {

    JacksonRecordParser parser = new JacksonRecordParser();

    @Mock
    JacksonRecordParserConfig config;

    @Mock
    JacksonPropertyResolver propertyResolver;

    @BeforeEach
    void setUp() {
        parser = new JacksonRecordParser(__ -> config, mapper, propertyResolver);
    }

    @Test
    void givenPointer_whenGetItemsArray_thenAllReturned() {

        given(config.getRecordsPointer()).willReturn(pointer);
        given(propertyResolver.getArrayAt(deserialize(itemArray), pointer)).willReturn(Stream.of(deserialize(item1), deserialize(item2)));
        parser.configure(emptyMap());

        assertThat(parser.getRecords(itemArray.getBytes())).containsExactly(deserialize(item1), deserialize(item2));
    }

    @Test
    void givenNoPointer_whenGetKey_thenEmpty() {

        given(config.getKeyPointer()).willReturn(emptyList());
        parser.configure(emptyMap());

        assertThat(parser.getKey(deserialize(item1))).isEmpty();
    }

    @Test
    void givenPointer_whenGetKey_thenKeyAsText() {

        given(config.getKeyPointer()).willReturn(singletonList(pointer));
        given(propertyResolver.getObjectAt(deserialize(item1), pointer)).willReturn(deserialize(item1).at("/k1"));
        parser.configure(emptyMap());

        assertThat(parser.getKey(deserialize(item1))).contains(property);
    }

    @Test
    void givenPointers_whenGetKey_thenKeyAsTextConcatenated() {

        JsonNode node = deserialize(item1);
        given(config.getKeyPointer()).willReturn(asList(pointer, pointer));
        given(propertyResolver.getObjectAt(node, pointer)).willReturn(node.at("/k1"));
        parser.configure(emptyMap());

        assertThat(parser.getKey(node)).contains(property + "+" + property);
    }

    @Test
    void givenPointer_whenGetValueText_thenValue() {

        given(config.getValuePointer()).willReturn(pointer);
        given(propertyResolver.getObjectAt(deserialize(item1), pointer)).willReturn(deserialize(item1).at("/k1"));
        parser.configure(emptyMap());

        assertThat(parser.getValue(deserialize(item1))).isEqualTo(property);
    }

    @Test
    void givenPointer_whenGetValueObject_thenValue() {

        given(config.getValuePointer()).willReturn(pointer);
        given(propertyResolver.getObjectAt(deserialize(item1), pointer)).willReturn(deserialize(item1));
        parser.configure(emptyMap());

        assertThat(parser.getValue(deserialize(item1))).isEqualTo(item1);
    }

    @Test
    void givenNoPointer_whenGetTimestamp_thenEmpty() {

        given(config.getTimestampPointer()).willReturn(Optional.empty());
        parser.configure(emptyMap());

        assertThat(parser.getTimestamp(deserialize(item1))).isEmpty();
    }

    @Test
    void givenPointer_whenGetTimestamp_thenKeyAsText() {

        given(config.getTimestampPointer()).willReturn(Optional.of(pointer));
        given(propertyResolver.getObjectAt(deserialize(item1), pointer)).willReturn(deserialize(item1));
        parser.configure(emptyMap());

        assertThat(parser.getTimestamp(deserialize(item1))).contains(deserialize(item1).asText());
    }

    @Test
    void givenPointer_whenGetOffset_thenOffset() {

        given(config.getOffsetPointers()).willReturn(ImmutableMap.of("key", pointer));
        given(propertyResolver.getObjectAt(deserialize(item1), pointer)).willReturn(deserialize(item1));
        parser.configure(emptyMap());

        assertThat(parser.getOffsets(deserialize(item1))).isEqualTo(ImmutableMap.of("key", deserialize(item1).asText()));
    }

    interface Fixture {
        ObjectMapper mapper = new ObjectMapper();
        String property = "v1";
        String item1 = "{\"k1\":\"" + property + "\"}";
        String item2 = "{\"k2\":\"v2\"}";
        String itemArray = "{\"items\":[" + item1 + "," + item2 + "]}";
        String itemNested = "{\"items\":" + item1 + "}";
        JsonPointer pointer = compile("/items");

        @SneakyThrows
        static JsonNode deserialize(String body) {
            return mapper.readTree(body);
        }
    }
}
