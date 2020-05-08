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
import com.github.castorm.kafka.connect.http.model.HttpResponse;
import com.github.castorm.kafka.connect.http.model.HttpResponseItem;
import com.github.castorm.kafka.connect.http.response.timestamp.spi.TimestampParser;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.stream.Stream;

import static com.github.castorm.kafka.connect.http.response.jackson.JacksonHttpResponseParserTest.Fixture.bytes;
import static com.github.castorm.kafka.connect.http.response.jackson.JacksonHttpResponseParserTest.Fixture.response;
import static com.github.castorm.kafka.connect.http.response.jackson.JacksonHttpResponseParserTest.Fixture.timestampIso;
import static com.github.castorm.kafka.connect.http.response.jackson.JacksonHttpResponseParserTest.Fixture.timestampParsed;
import static java.time.Instant.ofEpochMilli;
import static java.util.Collections.emptyMap;
import static java.util.stream.Stream.empty;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class JacksonHttpResponseParserTest {

    JacksonHttpResponseParser parser;

    @Mock
    JacksonHttpResponseParserConfig config;

    @Mock
    JacksonItemParser itemParser;

    @Mock
    TimestampParser timestampParser;

    @Mock
    JsonNode item;

    @BeforeEach
    void setUp() {
        parser = new JacksonHttpResponseParser(__ -> config);
        given(config.getItemParser()).willReturn(itemParser);
        given(config.getTimestampParser()).willReturn(timestampParser);
        parser.configure(emptyMap());
    }

    @Test
    void givenNoItems_thenEmpty() {

        givenItems(empty());

        assertThat(parser.parse(response)).isEmpty();
    }

    @Test
    void givenOneItem_thenKeyMapped() {

        givenItems(Stream.of(item));
        given(itemParser.getKey(item)).willReturn(Optional.of("key"));

        assertThat(parser.parse(response)).first().extracting(HttpResponseItem::getKey).isEqualTo("key");
    }

    @Test
    void givenOneItemWithNoNoKey_thenKeyDefault() {

        givenItems(Stream.of(item));
        given(itemParser.getKey(item)).willReturn(Optional.empty());

        assertThat(parser.parse(response)).first().extracting(HttpResponseItem::getKey).isNotNull();
    }

    @Test
    void givenOneItem_thenValueMapped() {

        givenItems(Stream.of(item));
        given(itemParser.getValue(item)).willReturn("value");

        assertThat(parser.parse(response)).first().extracting(HttpResponseItem::getValue).isEqualTo("value");
    }

    @Test
    void givenOneItem_thenTimestampMapped() {

        givenItems(Stream.of(item));
        given(itemParser.getTimestamp(item)).willReturn(Optional.of(timestampIso));
        given(timestampParser.parse(timestampIso)).willReturn(timestampParsed);

        assertThat(parser.parse(response)).first().extracting(HttpResponseItem::getTimestamp).isEqualTo(timestampParsed);
    }

    @Test
    void givenOneItemWithNoTimestamp_thenDefault() {

        givenItems(Stream.of(item));
        given(itemParser.getTimestamp(item)).willReturn(Optional.empty());

        assertThat(parser.parse(response)).first().extracting(HttpResponseItem::getTimestamp).isNotNull();
    }

    @Test
    void givenOneItem_thenOffsetMapped() {

        givenItems(Stream.of(item));
        given(itemParser.getOffsets(item)).willReturn(ImmutableMap.of("offset-key", "offset-value"));

        assertThat(parser.parse(response).stream().findFirst().get().getOffset()).containsEntry("offset-key", "offset-value");
    }

    @Test
    void givenOneItem_thenOffsetMappedWithTimestamp() {

        givenItems(Stream.of(item));
        given(itemParser.getOffsets(item)).willReturn(emptyMap());
        given(itemParser.getTimestamp(item)).willReturn(Optional.of(timestampIso));
        given(timestampParser.parse(timestampIso)).willReturn(timestampParsed);

        assertThat(parser.parse(response).stream().findFirst().get().getOffset()).containsEntry("timestamp_iso", timestampParsed.toString());
    }

    private void givenItems(Stream<JsonNode> items) {
        given(itemParser.getItems(eq(bytes))).willReturn(items);
    }

    interface Fixture {
        byte[] bytes = "bytes".getBytes();
        HttpResponse response = HttpResponse.builder().body(bytes).build();
        String timestampIso = ofEpochMilli(42L).toString();
        Instant timestampParsed = ofEpochMilli(43L);
    }
}
