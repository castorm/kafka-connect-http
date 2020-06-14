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
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.Configurable;
import org.apache.kafka.connect.source.SourceRecord;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static java.time.Instant.now;
import static java.util.stream.Collectors.toList;

@Slf4j
@RequiredArgsConstructor
public class HttpSourceTaskPartition implements Configurable {

    private final Function<Map<String, ?>, HttpSourceTaskPartitionConfig> configFactory;

    private Timer timer;

    private HttpRequestFactory requestFactory;

    private HttpClient requestExecutor;

    private HttpResponseParser responseParser;

    private SourceRecordSorter recordSorter;

    private SourceRecordFilterFactory recordFilterFactory;

    @Getter
    private Partition partition;

    @Getter
    private Offset offset;

    @Override
    public void configure(Map<String, ?> settings) {
        HttpSourceTaskPartitionConfig config = configFactory.apply(settings);
        partition = config.getPartition();
        timer = config.getTimer();
        requestFactory = config.getRequestFactory();
        requestExecutor = config.getClient();
        responseParser = config.getResponseParser();
        recordSorter = config.getRecordSorter();
        recordFilterFactory = config.getRecordFilterFactory();
        offset = config.getInitialOffset();
    }

    Long getRemainingMillis() {
        return timer.getRemainingMillis();
    }

    boolean isReady() {
        return getRemainingMillis() == 0;
    }

    List<SourceRecord> poll() {

        timer.reset(offset.getTimestamp().orElse(now()));

        HttpRequest request = requestFactory.createRequest(partition, offset);

        HttpResponse response = requestExecutor.execute(request);

        List<SourceRecord> records = responseParser.parse(response, partition);

        List<SourceRecord> filteredRecords = recordSorter.sort(records).stream()
                .filter(recordFilterFactory.create(offset))
                .collect(toList());

        log.debug("Read {}/{} new records from partition: {} with offset: {}", filteredRecords.size(), records.size(), partition.toMap(), offset.toMap());

        return filteredRecords;
    }

    void commit(Offset offset) {
        this.offset = offset;
    }
}
