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
import com.github.castorm.kafka.connect.http.record.model.KvRecord;
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

import static com.github.castorm.kafka.connect.http.response.jackson.JacksonKvRecordHttpResponseParserTest.Fixture.bytes;
import static com.github.castorm.kafka.connect.http.response.jackson.JacksonKvRecordHttpResponseParserTest.Fixture.response;
import static com.github.castorm.kafka.connect.http.response.jackson.JacksonKvRecordHttpResponseParserTest.Fixture.timestampIso;
import static com.github.castorm.kafka.connect.http.response.jackson.JacksonKvRecordHttpResponseParserTest.Fixture.timestampParsed;
import static java.time.Instant.ofEpochMilli;
import static java.util.Collections.emptyMap;
import static java.util.stream.Stream.empty;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class JacksonKvRecordHttpResponseParserTest {

    JacksonKvRecordHttpResponseParser parser;

    @Mock
    JacksonKvRecordHttpResponseParserConfig config;

    @Mock
    JacksonRecordParser recordParser;

    @Mock
    TimestampParser timestampParser;

    @Mock
    JsonNode record;

    @BeforeEach
    void setUp() {
        parser = new JacksonKvRecordHttpResponseParser(__ -> config);
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

        assertThat(parser.parse(response)).first().extracting(KvRecord::getKey).isEqualTo("key");
    }

    @Test
    void givenOneItemWithNoNoKey_thenKeyDefault() {

        givenRecords(Stream.of(record));
        given(recordParser.getKey(record)).willReturn(Optional.empty());

        assertThat(parser.parse(response)).first().extracting(KvRecord::getKey).isNotNull();
    }

    @Test
    void givenOneItem_thenValueMapped() {

        givenRecords(Stream.of(record));
        given(recordParser.getValue(record)).willReturn("value");

        assertThat(parser.parse(response)).first().extracting(KvRecord::getValue).isEqualTo("value");
    }

    @Test
    void givenOneItem_thenTimestampMapped() {

        givenRecords(Stream.of(record));
        given(recordParser.getTimestamp(record)).willReturn(Optional.of(timestampIso));
        given(timestampParser.parse(timestampIso)).willReturn(timestampParsed);

        assertThat(parser.parse(response)).first().extracting(KvRecord::getOffset).extracting(Offset::getTimestamp).isEqualTo(timestampParsed);
    }

    @Test
    void givenOneItemWithNoTimestamp_thenDefault() {

        givenRecords(Stream.of(record));
        given(recordParser.getTimestamp(record)).willReturn(Optional.empty());

        assertThat(parser.parse(response)).first().extracting(KvRecord::getOffset).extracting(Offset::getTimestamp).isNotNull();
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
