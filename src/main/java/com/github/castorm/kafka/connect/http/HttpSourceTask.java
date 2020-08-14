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

import com.github.castorm.kafka.connect.common.CollectionUtils;
import com.github.castorm.kafka.connect.common.control.Try;
import com.github.castorm.kafka.connect.http.model.Offset;
import com.github.castorm.kafka.connect.http.model.Partition;
import com.github.castorm.kafka.connect.timer.spi.ManagedThrottler;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.connect.source.SourceRecord;
import org.apache.kafka.connect.source.SourceTask;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static com.github.castorm.kafka.connect.common.VersionUtils.getVersion;

@RequiredArgsConstructor
public class HttpSourceTask extends SourceTask {

    private final Function<Map<String, String>, HttpSourceTaskConfig> configFactory;

    private ManagedThrottler throttler;

    private Map<Partition, HttpSourceTaskPartition> taskPartitions;

    public HttpSourceTask() {
        this(HttpSourceTaskConfig::new);
    }

    @Override
    public void start(Map<String, String> settings) {
        HttpSourceTaskConfig config = configFactory.apply(settings);
        throttler = config.getThrottler();
        taskPartitions = config.getPartitions();
        taskPartitions.values().forEach(this::initializeTaskPartition);
    }

    private void initializeTaskPartition(HttpSourceTaskPartition taskPartition) {
        Offset offset = Offset.of(context.offsetStorageReader().offset(taskPartition.getPartition().toMap()));
        if (!offset.isEmpty()) {
            taskPartition.commit(offset);
        }
    }

    @Override
    public List<SourceRecord> poll() throws InterruptedException {

        throttler.sleep();

        return taskPartitions.values().stream()
                .filter(HttpSourceTaskPartition::isReady)
                .map(partition -> Try.of(partition::poll))
                .reduce(Try.of(Collections::emptyList), Try.zip(CollectionUtils::concat))
                .getOrFail();
    }

    @Override
    public void commitRecord(SourceRecord record) {
        Offset offset = Offset.of(record.sourceOffset());
        Partition partition = Partition.of(record.sourcePartition());
        taskPartitions.get(partition).commit(offset);
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
