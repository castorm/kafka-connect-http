package com.github.castorm.kafka.connect.http.response.jackson;

/*-
 * #%L
 * kafka-connect-http-plugin
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
import static com.github.castorm.kafka.connect.http.response.jackson.JacksonItemParserTest.Fixture.deserialize;
import static com.github.castorm.kafka.connect.http.response.jackson.JacksonItemParserTest.Fixture.item1;
import static com.github.castorm.kafka.connect.http.response.jackson.JacksonItemParserTest.Fixture.item2;
import static com.github.castorm.kafka.connect.http.response.jackson.JacksonItemParserTest.Fixture.itemArray;
import static com.github.castorm.kafka.connect.http.response.jackson.JacksonItemParserTest.Fixture.itemNested;
import static com.github.castorm.kafka.connect.http.response.jackson.JacksonItemParserTest.Fixture.mapper;
import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class JacksonItemParserTest {

    JacksonItemParser parser = new JacksonItemParser();

    @Mock
    JacksonItemParserConfig config;

    @BeforeEach
    void setUp() {
        parser = new JacksonItemParser(__ -> config, () -> mapper);
    }

    @Test
    void givenPointer_whenGetItemsRoot_thenRoot() {

        given(config.getItemsPointer()).willReturn(compile("/"));
        parser.configure(emptyMap());

        assertThat(parser.getItems("{}".getBytes()))
                .containsExactly(deserialize("{}"));
    }

    @Test
    void givenPointer_whenGetItemsArray_thenAllReturned() {

        given(config.getItemsPointer()).willReturn(compile("/items"));
        parser.configure(emptyMap());

        assertThat(parser.getItems(itemArray.getBytes()))
                .containsExactly(deserialize(item1), deserialize(item2));
    }

    @Test
    void givenPointer_whenGetItemsNested_thenSingleReturned() {

        given(config.getItemsPointer()).willReturn(compile("/items"));
        parser.configure(emptyMap());

        assertThat(parser.getItems(itemNested.getBytes()))
                .containsExactly(deserialize(item1));
    }

    @Test
    void givenPointer_whenGetMissingItemsProperty_thenException() {

        given(config.getItemsPointer()).willReturn(compile("/missing"));
        parser.configure(emptyMap());

        assertThat(catchThrowable(() -> parser.getItems(itemArray.getBytes())))
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
