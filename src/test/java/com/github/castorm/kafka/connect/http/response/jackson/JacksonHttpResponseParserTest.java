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
import com.github.castorm.kafka.connect.http.model.HttpRecord;
import com.github.castorm.kafka.connect.http.model.HttpResponse;
import com.github.castorm.kafka.connect.http.model.Offset;
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
    JacksonHttpRecordParser recordParser;

    @Mock
    TimestampParser timestampParser;

    @Mock
    JsonNode record;

    @BeforeEach
    void setUp() {
        parser = new JacksonHttpResponseParser(__ -> config);
        given(config.getRecordParser()).willReturn(recordParser);
        given(config.getTimestampParser()).willReturn(timestampParser);
        parser.configure(emptyMap());
    }

    @Test
    void givenNoItems_thenEmpty() {

        givenRecords(empty());

        assertThat(parser.parse(response)).isEmpty();
    }

    @Test
    void givenOneItem_thenKeyMapped() {

        givenRecords(Stream.of(record));
        given(recordParser.getKey(record)).willReturn(Optional.of("key"));

        assertThat(parser.parse(response)).first().extracting(HttpRecord::getKey).isEqualTo("key");
    }

    @Test
    void givenOneItemWithNoNoKey_thenKeyDefault() {

        givenRecords(Stream.of(record));
        given(recordParser.getKey(record)).willReturn(Optional.empty());

        assertThat(parser.parse(response)).first().extracting(HttpRecord::getKey).isNotNull();
    }

    @Test
    void givenOneItem_thenValueMapped() {

        givenRecords(Stream.of(record));
        given(recordParser.getValue(record)).willReturn("value");

        assertThat(parser.parse(response)).first().extracting(HttpRecord::getValue).isEqualTo("value");
    }

    @Test
    void givenOneItem_thenTimestampMapped() {

        givenRecords(Stream.of(record));
        given(recordParser.getTimestamp(record)).willReturn(Optional.of(timestampIso));
        given(timestampParser.parse(timestampIso)).willReturn(timestampParsed);

        assertThat(parser.parse(response)).first().extracting(HttpRecord::getOffset).extracting(Offset::getTimestamp).isEqualTo(timestampParsed);
    }

    @Test
    void givenOneItemWithNoTimestamp_thenDefault() {

        givenRecords(Stream.of(record));
        given(recordParser.getTimestamp(record)).willReturn(Optional.empty());

        assertThat(parser.parse(response)).first().extracting(HttpRecord::getOffset).extracting(Offset::getTimestamp).isNotNull();
    }

    @Test
    void givenOneItem_thenOffsetMapped() {

        givenRecords(Stream.of(record));
        given(recordParser.getOffsets(record)).willReturn(ImmutableMap.of("offset-key", "offset-value"));

        assertThat(parser.parse(response).stream().findFirst().get().getOffset().toMap().get("offset-key")).isEqualTo("offset-value");
    }

    private void givenRecords(Stream<JsonNode> records) {
        given(recordParser.getRecords(eq(bytes))).willReturn(records);
    }

    interface Fixture {
        byte[] bytes = "bytes".getBytes();
        HttpResponse response = HttpResponse.builder().body(bytes).build();
        String timestampIso = ofEpochMilli(42L).toString();
        Instant timestampParsed = ofEpochMilli(43L);
    }
}
