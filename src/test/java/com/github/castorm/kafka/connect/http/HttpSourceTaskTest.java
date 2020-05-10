package com.github.castorm.kafka.connect.http;

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

import com.github.castorm.kafka.connect.http.client.spi.HttpClient;
import com.github.castorm.kafka.connect.http.model.HttpRequest;
import com.github.castorm.kafka.connect.http.model.HttpResponse;
import com.github.castorm.kafka.connect.http.model.HttpResponseItem;
import com.github.castorm.kafka.connect.http.model.Offset;
import com.github.castorm.kafka.connect.http.record.spi.SourceRecordMapper;
import com.github.castorm.kafka.connect.http.request.spi.HttpRequestFactory;
import com.github.castorm.kafka.connect.http.response.spi.HttpResponseFilterFactory;
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

import static com.github.castorm.kafka.connect.http.HttpSourceTaskTest.Fixture.item;
import static com.github.castorm.kafka.connect.http.HttpSourceTaskTest.Fixture.now;
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
    HttpResponseFilterFactory recordFilterFactory;

    @Mock
    SourceRecordMapper recordMapper;

    @BeforeEach
    void setUp() {
        task = new HttpSourceTask(__ -> config);
    }

    private void givenTaskConfiguration() {
        given(config.getThrottler()).willReturn(throttler);
        given(config.getRequestFactory()).willReturn(requestFactory);
        given(config.getClient()).willReturn(client);
        given(config.getResponseParser()).willReturn(responseParser);
        given(config.getRecordMapper()).willReturn(recordMapper);
        given(config.getResponseFilterFactory()).willReturn(recordFilterFactory);
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
    void givenTaskInitialized_whenStart_thenGetRecordMapper() {

        givenTaskConfiguration();
        task.initialize(getContext(offsetMap));

        task.start(emptyMap());

        then(config).should().getRecordMapper();
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

        task.commitRecord(record(offsetMap, now));

        assertThat(task.getOffset()).isEqualTo(Offset.of(offsetMap, now));
    }

    @Test
    void givenTaskStarted_whenPoll_thenThrottled() throws InterruptedException, IOException {

        givenTaskConfiguration();
        task.initialize(getContext(offsetMap));
        task.start(emptyMap());
        given(requestFactory.createRequest(offset)).willReturn(request);
        given(client.execute(request)).willReturn(response);
        given(responseParser.parse(response)).willReturn(asList(item));
        given(recordFilterFactory.create(offset)).willReturn(__ -> true);
        given(recordMapper.map(item)).willReturn(record(offsetMap, now));

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
        given(responseParser.parse(response)).willReturn(asList(item));
        given(recordFilterFactory.create(offset)).willReturn(__ -> true);
        given(recordMapper.map(item)).willReturn(record(offsetMap, now));

        assertThat(task.poll()).containsExactly(record(offsetMap, now));
    }

    @Test
    void givenTaskStarted_whenPoll_thenFilterFilters() throws InterruptedException, IOException {

        givenTaskConfiguration();
        task.initialize(getContext(offsetMap));
        task.start(emptyMap());
        given(requestFactory.createRequest(offset)).willReturn(request);
        given(client.execute(request)).willReturn(response);
        given(responseParser.parse(response)).willReturn(asList(item));
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

        verifyNoInteractions(throttler, requestFactory, responseParser, recordFilterFactory, recordMapper);
    }

    interface Fixture {
        Map<String, Object> offsetMap = ImmutableMap.of("k", "v");
        Map<String, String> offsetInitialMap = ImmutableMap.of("k2", "v2");
        Offset offset = Offset.of(offsetMap);
        HttpRequest request = HttpRequest.builder().build();
        HttpResponse response = HttpResponse.builder().build();
        HttpResponseItem item = HttpResponseItem.builder().build();
        Instant now = now();

        static SourceRecord record(Map<String, Object> offset, Instant timestamp) {
            return new SourceRecord(emptyMap(), offset, null, null, null, null, null, null, timestamp.toEpochMilli());
        }
    }
}
