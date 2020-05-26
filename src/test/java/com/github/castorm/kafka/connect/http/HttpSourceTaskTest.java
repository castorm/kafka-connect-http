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
import com.github.castorm.kafka.connect.http.record.spi.SourceRecordFilterFactory;
import com.github.castorm.kafka.connect.http.record.spi.SourceRecordSorter;
import com.github.castorm.kafka.connect.http.request.spi.HttpRequestFactory;
import com.github.castorm.kafka.connect.http.response.spi.HttpResponseParser;
import com.github.castorm.kafka.connect.throttle.spi.Throttler;
import com.google.common.collect.ImmutableMap;
import org.apache.kafka.connect.errors.RetriableException;
import org.apache.kafka.connect.source.SourceRecord;
import org.apache.kafka.connect.source.SourceTaskContext;
import org.apache.kafka.connect.storage.OffsetStorageReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;

import static com.github.castorm.kafka.connect.http.HttpSourceTaskTest.Fixture.offset;
import static com.github.castorm.kafka.connect.http.HttpSourceTaskTest.Fixture.offsetInitialMap;
import static com.github.castorm.kafka.connect.http.HttpSourceTaskTest.Fixture.offsetMap;
import static com.github.castorm.kafka.connect.http.HttpSourceTaskTest.Fixture.record;
import static com.github.castorm.kafka.connect.http.HttpSourceTaskTest.Fixture.request;
import static com.github.castorm.kafka.connect.http.HttpSourceTaskTest.Fixture.response;
import static java.time.Instant.now;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class HttpSourceTaskTest {

    HttpSourceTask task;

    @Mock
    HttpSourceConnectorConfig config;

    @Mock
    Throttler throttler;

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
        task = new HttpSourceTask(__ -> config);
    }

    private void givenTaskConfiguration() {
        given(config.getThrottler()).willReturn(throttler);
        given(config.getRequestFactory()).willReturn(requestFactory);
        given(config.getClient()).willReturn(client);
        given(config.getResponseParser()).willReturn(responseParser);
        given(config.getRecordSorter()).willReturn(recordSorter);
        given(config.getRecordFilterFactory()).willReturn(recordFilterFactory);
    }

    private static SourceTaskContext getContext(Map<String, Object> offset) {
        SourceTaskContext context = mock(SourceTaskContext.class);
        OffsetStorageReader offsetStorageReader = mock(OffsetStorageReader.class);
        given(context.offsetStorageReader()).willReturn(offsetStorageReader);
        given(offsetStorageReader.offset(any())).willReturn(offset);
        return context;
    }

    @Test
    void givenTaskInitializedWithRestoredOffset_whenStart_thenLastOffsetIsRestored() {

        givenTaskConfiguration();
        task.initialize(getContext(offsetMap));

        task.start(emptyMap());

        assertThat(task.getOffset()).isEqualTo(Offset.of(offsetMap));
    }

    @Test
    void givenTaskInitializedWithoutRestoredOffsetButWithInitialOffset_whenStart_thenLastOffsetIsInitial() {

        givenTaskConfiguration();
        given(config.getInitialOffset()).willReturn(offsetInitialMap);
        task.initialize(getContext(emptyMap()));

        task.start(emptyMap());

        assertThat(task.getOffset()).isEqualTo(Offset.of(offsetInitialMap));
    }

    @Test
    void givenTaskInitialized_whenStart_thenGetPollIntervalMillis() {

        givenTaskConfiguration();
        task.initialize(getContext(offsetMap));

        task.start(emptyMap());

        then(config).should().getThrottler();
    }

    @Test
    void givenTaskInitialized_whenStart_thenGetRequestFactory() {

        givenTaskConfiguration();
        task.initialize(getContext(offsetMap));

        task.start(emptyMap());

        then(config).should().getRequestFactory();
    }

    @Test
    void givenTaskInitialized_whenStart_thenGetClient() {

        givenTaskConfiguration();
        task.initialize(getContext(offsetMap));

        task.start(emptyMap());

        then(config).should().getClient();
    }

    @Test
    void givenTaskInitialized_whenStart_thenGetResponseParser() {

        givenTaskConfiguration();
        task.initialize(getContext(offsetMap));

        task.start(emptyMap());

        then(config).should().getResponseParser();
    }

    @Test
    void givenTaskStarted_whenCommitRecord_thenOffsetUpdated() {

        givenTaskConfiguration();
        task.initialize(getContext(offsetMap));
        task.start(emptyMap());
        reset(requestFactory);

        task.commitRecord(record(offsetMap));

        assertThat(task.getOffset()).isEqualTo(Offset.of(offsetMap));
    }

    @Test
    void givenTaskStarted_whenPoll_thenThrottled() throws InterruptedException, IOException {

        givenTaskConfiguration();
        task.initialize(getContext(offsetMap));
        task.start(emptyMap());
        given(requestFactory.createRequest(offset)).willReturn(request);
        given(client.execute(request)).willReturn(response);
        given(responseParser.parse(response)).willReturn(asList(record(offsetMap)));
        given(recordFilterFactory.create(offset)).willReturn(__ -> true);

        task.poll();

        then(throttler).should().throttle(offset);
    }

    @Test
    void givenTaskStarted_whenPoll_thenResultsReturned() throws InterruptedException, IOException {

        givenTaskConfiguration();
        task.initialize(getContext(offsetMap));
        task.start(emptyMap());
        given(requestFactory.createRequest(offset)).willReturn(request);
        given(client.execute(request)).willReturn(response);
        given(responseParser.parse(response)).willReturn(asList(record(offsetMap)));
        given(recordSorter.sort(asList(record(offsetMap)))).willReturn(asList(record(offsetMap)));
        given(recordFilterFactory.create(offset)).willReturn(__ -> true);

        assertThat(task.poll()).containsExactly(record(offsetMap));
    }

    @Test
    void givenTaskStarted_whenPoll_thenResultsSorted() throws InterruptedException, IOException {

        givenTaskConfiguration();
        task.initialize(getContext(offsetMap));
        task.start(emptyMap());
        given(requestFactory.createRequest(offset)).willReturn(request);
        given(client.execute(request)).willReturn(response);
        given(responseParser.parse(response)).willReturn(asList(record(offsetMap)));
        given(recordSorter.sort(asList(record(offsetMap)))).willReturn(asList(record(offsetMap), record(offsetMap)));
        given(recordFilterFactory.create(offset)).willReturn(__ -> true);

        assertThat(task.poll()).containsExactly(record(offsetMap), record(offsetMap));
    }

    @Test
    void givenTaskStarted_whenPoll_thenFilterFilters() throws InterruptedException, IOException {

        givenTaskConfiguration();
        task.initialize(getContext(offsetMap));
        task.start(emptyMap());
        given(requestFactory.createRequest(offset)).willReturn(request);
        given(client.execute(request)).willReturn(response);
        given(responseParser.parse(response)).willReturn(asList(record(offsetMap)));
        given(recordFilterFactory.create(offset)).willReturn(__ -> false);

        assertThat(task.poll()).isEmpty();
    }

    @Test
    void givenTaskStartedAndExecuteFails_whenPoll_thenRetriableException() throws IOException {

        givenTaskConfiguration();
        task.initialize(getContext(offsetMap));
        task.start(emptyMap());
        given(requestFactory.createRequest(offset)).willReturn(request);
        given(client.execute(request)).willThrow(new IOException());

        assertThat(catchThrowable(() -> task.poll())).isInstanceOf(RetriableException.class);
    }

    @Test
    void whenGetVersion_thenNotEmpty() {

        assertThat(task.version()).isNotEmpty();
    }

    @Test
    void whenStop_thenNothingHappens() {

        task.stop();

        verifyNoInteractions(throttler, requestFactory, responseParser, recordFilterFactory);
    }

    interface Fixture {
        Instant now = now();
        String key = "customKey";
        Map<String, Object> offsetMap = ImmutableMap.of("custom", "value", "key", key, "timestamp", now.toString());
        Map<String, String> offsetInitialMap = ImmutableMap.of("k2", "v2");
        Offset offset = Offset.of(offsetMap);
        HttpRequest request = HttpRequest.builder().build();
        HttpResponse response = HttpResponse.builder().build();

        static SourceRecord record(Map<String, Object> offset) {
            return new SourceRecord(emptyMap(), offset, null, null, null, null, null, null, now.toEpochMilli());
        }
    }
}
