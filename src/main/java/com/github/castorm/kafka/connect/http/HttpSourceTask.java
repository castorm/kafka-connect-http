package com.github.castorm.kafka.connect.http;

import com.github.castorm.kafka.connect.http.client.spi.HttpClient;
import com.github.castorm.kafka.connect.http.model.HttpRequest;
import com.github.castorm.kafka.connect.http.model.HttpResponse;
import com.github.castorm.kafka.connect.http.record.spi.SourceRecordMapper;
import com.github.castorm.kafka.connect.http.request.spi.HttpRequestFactory;
import com.github.castorm.kafka.connect.http.response.spi.HttpResponseParser;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.connect.errors.RetriableException;
import org.apache.kafka.connect.source.SourceRecord;
import org.apache.kafka.connect.source.SourceTask;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.github.castorm.kafka.connect.common.VersionUtils.getVersion;
import static java.lang.System.currentTimeMillis;
import static java.lang.Thread.sleep;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toList;

@Slf4j
public class HttpSourceTask extends SourceTask {

    private HttpRequestFactory requestFactory;

    private HttpClient requestExecutor;

    private HttpResponseParser responseParser;

    private SourceRecordMapper recordMapper;

    private Long pollIntervalMillis = 1000L;

    private Long lastPollMillis = 0L;

    private boolean upToDate = true;

    @Override
    public void start(Map<String, String> settings) {

        HttpSourceConnectorConfig config = new HttpSourceConnectorConfig(settings);

        Map<String, ?> offset = context.offsetStorageReader().offset(emptyMap());

        pollIntervalMillis = config.getPollIntervalMillis();
        requestFactory = config.getRequestFactory();
        requestFactory.setOffset(offset);

        requestExecutor = config.getClient();

        responseParser = config.getResponseParser();

        recordMapper = config.getRecordMapper();
    }

    @Override
    public List<SourceRecord> poll() throws InterruptedException {

        if (upToDate) {
            awaitNextTick();
        }

        HttpRequest request = requestFactory.createRequest();

        HttpResponse response = execute(request);

        List<SourceRecord> records = responseParser.parse(response).stream()
                .map(recordMapper::map)
                .collect(toList());

        upToDate = records.isEmpty();

        return records;
    }

    private void awaitNextTick() throws InterruptedException {
        long remainingMillis = pollIntervalMillis - (currentTimeMillis() - lastPollMillis);
        if (remainingMillis > 0) {
            sleep(remainingMillis);
        }
        lastPollMillis = currentTimeMillis();
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

        log.debug("Committed {}", record);

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
