package com.github.castorm.kafka.connect.http;

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

import com.github.castorm.kafka.connect.http.client.spi.HttpClient;
import com.github.castorm.kafka.connect.http.model.HttpRequest;
import com.github.castorm.kafka.connect.http.model.HttpResponse;
import com.github.castorm.kafka.connect.http.model.Offset;
import com.github.castorm.kafka.connect.http.model.Partition;
import com.github.castorm.kafka.connect.http.record.spi.SourceRecordFilterFactory;
import com.github.castorm.kafka.connect.http.record.spi.SourceRecordSorter;
import com.github.castorm.kafka.connect.http.request.spi.HttpRequestFactory;
import com.github.castorm.kafka.connect.http.response.spi.HttpResponseParser;
import com.github.castorm.kafka.connect.timer.spi.Timer;
import com.google.common.collect.ImmutableMap;
import org.apache.kafka.connect.source.SourceRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Map;

import static com.github.castorm.kafka.connect.http.HttpSourceTaskPartitionTest.Fixture.key;
import static com.github.castorm.kafka.connect.http.HttpSourceTaskPartitionTest.Fixture.now;
import static com.github.castorm.kafka.connect.http.HttpSourceTaskPartitionTest.Fixture.offset;
import static com.github.castorm.kafka.connect.http.HttpSourceTaskPartitionTest.Fixture.offsetMap;
import static com.github.castorm.kafka.connect.http.HttpSourceTaskPartitionTest.Fixture.partition;
import static com.github.castorm.kafka.connect.http.HttpSourceTaskPartitionTest.Fixture.record;
import static com.github.castorm.kafka.connect.http.HttpSourceTaskPartitionTest.Fixture.request;
import static com.github.castorm.kafka.connect.http.HttpSourceTaskPartitionTest.Fixture.response;
import static java.time.Instant.now;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class HttpSourceTaskPartitionTest {

    HttpSourceTaskPartition task;

    @Mock
    HttpSourceTaskPartitionConfig config;

    @Mock
    Timer timer;

    @Mock
    HttpRequestFactory requestFactory;

    @Mock
    HttpClient client;

    @Mock
    HttpResponseParser responseParser;

    @Mock
    SourceRecordSorter recordSorter;

    @Mock
    SourceRecordFilterFactory recordFilterFactory;

    @BeforeEach
    void setUp() {
        given(config.getTimer()).willReturn(timer);
        given(config.getPartition()).willReturn(partition);
        given(config.getInitialOffset()).willReturn(offset);
        given(config.getRequestFactory()).willReturn(requestFactory);
        given(config.getClient()).willReturn(client);
        given(config.getResponseParser()).willReturn(responseParser);
        given(config.getRecordSorter()).willReturn(recordSorter);
        given(config.getRecordFilterFactory()).willReturn(recordFilterFactory);
        task = new HttpSourceTaskPartition(__ -> config);
        task.configure(emptyMap());
    }

    @Test
    void givenTaskStarted_whenCommitRecord_thenOffsetUpdated() {

        task.commit(Offset.of(record(offsetMap).sourceOffset()));

        assertThat(task.getOffset()).isEqualTo(Offset.of(offsetMap));
    }

    @Test
    void givenOffset_whenGetRemainingMillis_thenDelegatedToTimer() {

        task.commit(Offset.of(emptyMap(), key, now));
        given(timer.getRemainingMillis()).willReturn(42L);

        assertThat(task.getRemainingMillis()).isEqualTo(42L);
    }

    @Test
    void givenOffsetWithoutTimestamp_whenGetRemainingMillis_thenZero() {

        task.commit(Offset.of(emptyMap(), key));

        assertThat(task.getRemainingMillis()).isEqualTo(0L);
    }

    @Test
    void givenOffset_whenPoll_thenTimerReset() {

        given(requestFactory.createRequest(partition, offset)).willReturn(request);
        given(client.execute(request)).willReturn(response);
        given(responseParser.parse(response, partition)).willReturn(asList(record(offsetMap)));
        given(recordSorter.sort(asList(record(offsetMap)))).willReturn(asList(record(offsetMap)));
        given(recordFilterFactory.create(offset)).willReturn(__ -> true);

        task.poll();

        then(timer).should().reset(offset.getTimestamp().get());
    }

    @Test
    void givenOffset_whenPoll_thenResultsReturned() {

        given(requestFactory.createRequest(partition, offset)).willReturn(request);
        given(client.execute(request)).willReturn(response);
        given(responseParser.parse(response, partition)).willReturn(asList(record(offsetMap)));
        given(recordSorter.sort(asList(record(offsetMap)))).willReturn(asList(record(offsetMap)));
        given(recordFilterFactory.create(offset)).willReturn(__ -> true);

        assertThat(task.poll()).containsExactly(record(offsetMap));
    }

    @Test
    void givenTaskStarted_whenPoll_thenResultsSorted() {

        given(requestFactory.createRequest(partition, offset)).willReturn(request);
        given(client.execute(request)).willReturn(response);
        given(responseParser.parse(response, partition)).willReturn(asList(record(offsetMap)));
        given(recordSorter.sort(asList(record(offsetMap)))).willReturn(asList(record(offsetMap), record(offsetMap)));
        given(recordFilterFactory.create(offset)).willReturn(__ -> true);

        assertThat(task.poll()).containsExactly(record(offsetMap), record(offsetMap));
    }

    @Test
    void givenTaskStarted_whenPoll_thenFilterFilters() {

        given(requestFactory.createRequest(partition, offset)).willReturn(request);
        given(client.execute(request)).willReturn(response);
        given(responseParser.parse(response, partition)).willReturn(asList(record(offsetMap)));
        given(recordFilterFactory.create(offset)).willReturn(__ -> false);

        assertThat(task.poll()).isEmpty();
    }

    interface Fixture {
        Instant now = now();
        String key = "customKey";
        Map<String, Object> offsetMap = ImmutableMap.of("custom", "value", "key", key, "timestamp", now.toString());
        Offset offset = Offset.of(offsetMap);
        Partition partition = Partition.of(ImmutableMap.of("k", "v"));
        HttpRequest request = HttpRequest.builder().build();
        HttpResponse response = HttpResponse.builder().build();

        static SourceRecord record(Map<String, Object> offset) {
            return new SourceRecord(emptyMap(), offset, null, null, null, null, null, null, now.toEpochMilli());
        }
    }
}
