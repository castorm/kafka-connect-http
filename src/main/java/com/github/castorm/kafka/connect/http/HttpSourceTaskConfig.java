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

import com.github.castorm.kafka.connect.http.model.Partition;
import com.github.castorm.kafka.connect.http.partition.ConfiguredPartitionProvider;
import com.github.castorm.kafka.connect.http.partition.spi.PartitionProvider;
import com.github.castorm.kafka.connect.timer.CompositeTimer;
import com.github.castorm.kafka.connect.timer.TimerThrottler;
import com.github.castorm.kafka.connect.timer.spi.ManagedThrottler;
import com.github.castorm.kafka.connect.timer.spi.Timer;
import lombok.Getter;
import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.common.config.ConfigDef;

import java.util.Map;

import static java.util.Collections.emptyMap;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.apache.kafka.common.config.ConfigDef.Importance.LOW;
import static org.apache.kafka.common.config.ConfigDef.Type.CLASS;

@Getter
class HttpSourceTaskConfig extends AbstractConfig {

    private static final String PARTITION_PROVIDER = "http.partitions.provider";

    private final Map<Partition, HttpSourceTaskPartition> partitions;
    private final ManagedThrottler throttler;

    HttpSourceTaskConfig(Map<String, ?> originals) {
        super(config(), originals);

        PartitionProvider partitionProvider = getConfiguredInstance(PARTITION_PROVIDER, PartitionProvider.class);

        partitions = partitionProvider.getPartitions().stream()
                .map(partition -> createTaskPartition(originals, partition))
                .collect(toMap(HttpSourceTaskPartition::getPartition, identity()));

        throttler = new TimerThrottler(new CompositeTimer(partitions.values().stream()
                .map(partition -> (Timer) partition::getRemainingMillis)
                .collect(toList())));
    }

    private static HttpSourceTaskPartition createTaskPartition(Map<String, ?> originals, Partition partition) {
        HttpSourceTaskPartitionConfig config = new HttpSourceTaskPartitionConfig(partition, originals);
        HttpSourceTaskPartition taskPartition = new HttpSourceTaskPartition(__ -> config);
        taskPartition.configure(emptyMap());
        return taskPartition;
    }

    public static ConfigDef config() {
        ConfigDef config = new ConfigDef()
                .define(PARTITION_PROVIDER, CLASS, ConfiguredPartitionProvider.class, LOW, "Task Partition Provider");
        config.embed("", "", 0, HttpSourceTaskPartitionConfig.config());
        return config;
    }
}
