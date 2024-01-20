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

import com.github.castorm.kafka.connect.http.ack.ConfirmationWindow;
import com.github.castorm.kafka.connect.http.client.spi.HttpClient;
import com.github.castorm.kafka.connect.http.model.HttpRequest;
import com.github.castorm.kafka.connect.http.model.HttpResponse;
import com.github.castorm.kafka.connect.http.model.Offset;
import com.github.castorm.kafka.connect.http.record.spi.SourceRecordFilterFactory;
import com.github.castorm.kafka.connect.http.record.spi.SourceRecordSorter;
import com.github.castorm.kafka.connect.http.request.spi.HttpRequestFactory;
import com.github.castorm.kafka.connect.http.request.template.TemplateHttpRequestFactoryConfig;
import com.github.castorm.kafka.connect.http.response.spi.HttpResponseParser;
import com.github.castorm.kafka.connect.timer.TimerThrottler;
import edu.emory.mathcs.backport.java.util.Collections;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.config.ConfigException;
import org.apache.kafka.connect.errors.RetriableException;
import org.apache.kafka.connect.source.SourceRecord;
import org.apache.kafka.connect.source.SourceTask;

import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.function.Function;

import static com.github.castorm.kafka.connect.common.VersionUtils.getVersion;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

@Slf4j
public class HttpSourceTask extends SourceTask {

    private List<HttpSourceTaskSingleIndex> tasks;
    Executor

    @Override
    public void start(Map<String, String> settings) {
        String indexIncludeList = settings.get(HttpSourceConnectorConfig.INDEX_INCLUDE_LIST);
        if (null == indexIncludeList) {
            throw new ConfigException(HttpSourceConnectorConfig.INDEX_INCLUDE_LIST + " is required");
        }

        String originalUrl = settings.get(TemplateHttpRequestFactoryConfig.URL);

        List<String> indexes = List.of(indexIncludeList.split(","));
        int idx = 0;
        for (String index : indexes) {
            log.info("Initializing task {} for index {}", idx++, index);
            Map<String, String> taskSettings = new HashMap<>();
            taskSettings.putAll(settings);
            taskSettings.put(TemplateHttpRequestFactoryConfig.URL, originalUrl.replace("<INDEX>", index));
            HttpSourceTaskSingleIndex task = new HttpSourceTaskSingleIndex();
            task.start(taskSettings);
            tasks.add(task);
        }
    }


    @Override
    public List<SourceRecord> poll() throws InterruptedException {

        //call tasks in parallel
        List<List<SourceRecord>> records = tasks.parallelStream()
                .map(HttpSourceTaskSingleIndex::poll)
                .collect(toList());
    }

    private HttpResponse execute(HttpRequest request) {
        try {
            return requestExecutor.execute(request);
        } catch (IOException e) {
            throw new RetriableException(e);
        }
    }

    private static List<Map<String, ?>> extractOffsets(List<SourceRecord> recordsToSend) {
        return recordsToSend.stream()
                .map(SourceRecord::sourceOffset)
                .collect(toList());
    }

    @Override
    public void commitRecord(SourceRecord record, RecordMetadata metadata) {
        confirmationWindow.confirm(record.sourceOffset());
    }

    @Override
    public void commit() {
        offset = confirmationWindow.getLowWatermarkOffset()
                .map(Offset::of)
                .orElse(offset);

        log.debug("Offset set to {}", offset);
    }

    @Override
    public void stop() {
        // Nothing to do, no resources to release
    }

    @Override
    public String version() {
        return getVersion();
    }
}
