package com.github.castorm.kafka.connect.http;

import com.github.castorm.kafka.connect.http.client.spi.HttpClient;
import com.github.castorm.kafka.connect.http.model.HttpRequest;
import com.github.castorm.kafka.connect.http.model.HttpResponse;
import com.github.castorm.kafka.connect.http.poll.spi.PollInterceptor;
import com.github.castorm.kafka.connect.http.record.spi.SourceRecordMapper;
import com.github.castorm.kafka.connect.http.request.spi.HttpRequestFactory;
import com.github.castorm.kafka.connect.http.response.spi.HttpResponseParser;
import org.apache.kafka.connect.errors.RetriableException;
import org.apache.kafka.connect.source.SourceRecord;
import org.apache.kafka.connect.source.SourceTask;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static com.github.castorm.kafka.connect.common.VersionUtils.getVersion;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toList;

public class HttpSourceTask extends SourceTask {

    private final Function<Map<String, String>, HttpSourceConnectorConfig> configFactory;

    private PollInterceptor pollInterceptor;

    private HttpRequestFactory requestFactory;

    private HttpClient requestExecutor;

    private HttpResponseParser responseParser;

    private SourceRecordMapper recordMapper;

    HttpSourceTask(Function<Map<String, String>, HttpSourceConnectorConfig> configFactory) {
        this.configFactory = configFactory;
    }

    public HttpSourceTask() {
        this(HttpSourceConnectorConfig::new);
    }

    @Override
    public void start(Map<String, String> settings) {

        HttpSourceConnectorConfig config = configFactory.apply(settings);

        Map<String, ?> offset = context.offsetStorageReader().offset(emptyMap());

        pollInterceptor = config.getPollInterceptor();
        requestFactory = config.getRequestFactory();
        requestFactory.setOffset(offset);
        requestExecutor = config.getClient();
        responseParser = config.getResponseParser();
        recordMapper = config.getRecordMapper();
    }

    @Override
    public List<SourceRecord> poll() throws InterruptedException {

        pollInterceptor.beforePoll();

        HttpRequest request = requestFactory.createRequest();

        HttpResponse response = execute(request);

        List<SourceRecord> records = responseParser.parse(response).stream()
                .map(recordMapper::map)
                .collect(toList());

        pollInterceptor.afterPoll(records);

        return records;
    }

    private HttpResponse execute(HttpRequest request) {
        try {
            return requestExecutor.execute(request);
        } catch (IOException e) {
            throw new RetriableException(e);
        }
    }

    @Override
    public void commitRecord(SourceRecord record) {

        requestFactory.setOffset(record.sourceOffset());
    }

    @Override
    public void stop() {
    }

    @Override
    public String version() {
        return getVersion(getClass());
    }
}
