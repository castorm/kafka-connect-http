package com.github.castorm.kafka.connect.http.response;

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

import com.github.castorm.kafka.connect.http.model.HttpResponse;
import com.github.castorm.kafka.connect.http.record.model.KvRecord;
import com.github.castorm.kafka.connect.http.record.spi.KvSourceRecordMapper;
import com.github.castorm.kafka.connect.http.response.spi.KvRecordHttpResponseParser;
import org.apache.kafka.connect.source.SourceRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.github.castorm.kafka.connect.http.response.KvHttpResponseParserTest.Fixture.record;
import static com.github.castorm.kafka.connect.http.response.KvHttpResponseParserTest.Fixture.response;
import static com.github.castorm.kafka.connect.http.response.KvHttpResponseParserTest.Fixture.sourceRecord;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class KvHttpResponseParserTest {

    KvHttpResponseParser parser;

    @Mock
    KvHttpResponseParserConfig config;

    @Mock
    KvRecordHttpResponseParser recordParser;

    @Mock
    KvSourceRecordMapper recordFactory;

    @BeforeEach
    void setUp() {
        parser = new KvHttpResponseParser(__ -> config);
        given(config.getRecordParser()).willReturn(recordParser);
        given(config.getRecordMapper()).willReturn(recordFactory);
        parser.configure(emptyMap());
    }

    @Test
    void givenEmptyList_whenParse_thenEmpty() {

        given(recordParser.parse(response)).willReturn(emptyList());

        assertThat(parser.parse("dummy-endpoint", response)).isEmpty();
    }

    @Test
    void givenList_whenParse_thenItemsMapped() {

        given(recordParser.parse(response)).willReturn(singletonList(record));

        parser.parse("dummy-endpoint", response);

        then(recordFactory).should().map("dummy-endpoint", record);
    }

    @Test
    void givenEmptyList_whenParse_thenItemsMappedReturned() {

        given(recordParser.parse(response)).willReturn(singletonList(record));
        given(recordFactory.map("dummy-endpoint", record)).willReturn(sourceRecord);

        assertThat(parser.parse("dummy-endpoint", response)).containsExactly(sourceRecord);
    }

    interface Fixture {
        HttpResponse response = HttpResponse.builder().build();
        KvRecord record = KvRecord.builder().build();
        SourceRecord sourceRecord = new SourceRecord(null, null, null, null, null);
    }
}
