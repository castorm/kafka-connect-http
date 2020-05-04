package com.github.castorm.kafka.connect.http;

import com.github.castorm.kafka.connect.http.client.spi.HttpClient;
import com.github.castorm.kafka.connect.http.model.HttpRequest;
import com.github.castorm.kafka.connect.http.model.HttpResponse;
import com.github.castorm.kafka.connect.http.model.HttpResponseItem;
import com.github.castorm.kafka.connect.http.poll.spi.PollInterceptor;
import com.github.castorm.kafka.connect.http.record.spi.SourceRecordMapper;
import com.github.castorm.kafka.connect.http.request.spi.HttpRequestFactory;
import com.github.castorm.kafka.connect.http.response.spi.HttpResponseParser;
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
import java.util.Map;

import static com.github.castorm.kafka.connect.http.HttpSourceTaskTest.Fixture.item;
import static com.github.castorm.kafka.connect.http.HttpSourceTaskTest.Fixture.offset;
import static com.github.castorm.kafka.connect.http.HttpSourceTaskTest.Fixture.record;
import static com.github.castorm.kafka.connect.http.HttpSourceTaskTest.Fixture.request;
import static com.github.castorm.kafka.connect.http.HttpSourceTaskTest.Fixture.response;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;

@ExtendWith(MockitoExtension.class)
class HttpSourceTaskTest {

    HttpSourceTask task;

    @Mock
    HttpSourceConnectorConfig config;

    @Mock
    PollInterceptor pollInterceptor;

    @Mock
    HttpRequestFactory requestFactory;

    @Mock
    HttpClient client;

    @Mock
    HttpResponseParser responseParser;

    @Mock
    SourceRecordMapper recordMapper;

    @BeforeEach
    void setUp() {
        given(config.getPollInterceptor()).willReturn(pollInterceptor);
        given(config.getRequestFactory()).willReturn(requestFactory);
        given(config.getClient()).willReturn(client);
        given(config.getResponseParser()).willReturn(responseParser);
        given(config.getRecordMapper()).willReturn(recordMapper);
        task = new HttpSourceTask(__ -> config);
    }

    private static SourceTaskContext getContext(Map<String, Object> offset) {
        SourceTaskContext context = mock(SourceTaskContext.class);
        OffsetStorageReader offsetStorageReader = mock(OffsetStorageReader.class);
        given(context.offsetStorageReader()).willReturn(offsetStorageReader);
        given(offsetStorageReader.offset(any())).willReturn(offset);
        return context;
    }

    @Test
    void givenTaskInitialized_whenStart_thenSetOffsetOnRequestFactory() {

        task.initialize(getContext(offset));

        task.start(emptyMap());

        then(requestFactory).should().setOffset(offset);
    }

    @Test
    void givenTaskInitialized_whenStart_thenGetPollIntervalMillis() {

        task.initialize(getContext(offset));

        task.start(emptyMap());

        then(config).should().getPollInterceptor();
    }

    @Test
    void givenTaskInitialized_whenStart_thenGetRequestFactory() {

        task.initialize(getContext(offset));

        task.start(emptyMap());

        then(config).should().getRequestFactory();
    }

    @Test
    void givenTaskInitialized_whenStart_thenGetClient() {

        task.initialize(getContext(offset));

        task.start(emptyMap());

        then(config).should().getClient();
    }

    @Test
    void givenTaskInitialized_whenStart_thenGetRecordMapper() {

        task.initialize(getContext(offset));

        task.start(emptyMap());

        then(config).should().getRecordMapper();
    }

    @Test
    void givenTaskInitialized_whenStart_thenGetResponseParser() {

        task.initialize(getContext(offset));

        task.start(emptyMap());

        then(config).should().getResponseParser();
    }

    @Test
    void givenTaskStarted_whenCommitRecord_thenSetOffsetOnRequestFactory() {

        task.initialize(getContext(offset));
        task.start(emptyMap());
        reset(requestFactory);

        task.commitRecord(record(offset));

        then(requestFactory).should().setOffset(offset);
    }

    @Test
    void givenTaskStarted_whenPoll_thenResultsReturned() throws InterruptedException, IOException {

        task.initialize(getContext(offset));
        task.start(emptyMap());
        given(requestFactory.createRequest()).willReturn(request);
        given(client.execute(request)).willReturn(response);
        given(responseParser.parse(response)).willReturn(asList(item));
        given(recordMapper.map(item)).willReturn(record(offset));

        assertThat(task.poll()).containsExactly(record(offset));
    }

    @Test
    void givenTaskStarted_whenPoll_thenPollInterceptorBefore() throws InterruptedException, IOException {

        task.initialize(getContext(offset));
        task.start(emptyMap());
        given(requestFactory.createRequest()).willReturn(request);
        given(client.execute(request)).willReturn(response);
        given(responseParser.parse(response)).willReturn(asList(item));
        given(recordMapper.map(item)).willReturn(record(offset));

        task.poll();

        then(pollInterceptor).should().beforePoll();
    }

    @Test
    void givenTaskStarted_whenPoll_thenPollInterceptorAfterWithRecords() throws InterruptedException, IOException {

        task.initialize(getContext(offset));
        task.start(emptyMap());
        given(requestFactory.createRequest()).willReturn(request);
        given(client.execute(request)).willReturn(response);
        given(responseParser.parse(response)).willReturn(asList(item));
        given(recordMapper.map(item)).willReturn(record(offset));

        task.poll();

        then(pollInterceptor).should().afterPoll(asList(record(offset)));
    }

    @Test
    void givenTaskStartedAndExecuteFails_whenPoll_thenRetriableException() throws IOException {

        task.initialize(getContext(offset));
        task.start(emptyMap());
        given(requestFactory.createRequest()).willReturn(request);
        given(client.execute(request)).willThrow(new IOException());

        assertThat(catchThrowable(() -> task.poll())).isInstanceOf(RetriableException.class);
    }

    interface Fixture {
        Map<String, Object> offset = ImmutableMap.of("k", "v");
        HttpRequest request = HttpRequest.builder().build();
        HttpResponse response = HttpResponse.builder().build();
        HttpResponseItem item = HttpResponseItem.builder().build();

        static SourceRecord record(Map<String, Object> offset) {
            return new SourceRecord(emptyMap(), offset, null, null, null);
        }
    }
}
