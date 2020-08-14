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
import com.github.castorm.kafka.connect.timer.CompositeTimer;
import com.github.castorm.kafka.connect.timer.TimerThrottler;
import com.github.castorm.kafka.connect.timer.spi.ManagedThrottler;
import com.github.castorm.kafka.connect.timer.spi.Timer;
import lombok.Getter;
import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.common.config.ConfigDef;

import java.util.Map;

import static com.github.castorm.kafka.connect.common.ConfigUtils.breakDownList;
import static com.github.castorm.kafka.connect.common.ConfigUtils.replaceKey;
import static java.util.Collections.emptyMap;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.apache.kafka.common.config.ConfigDef.Importance.LOW;
import static org.apache.kafka.common.config.ConfigDef.Type.STRING;

@Getter
class HttpSourceTaskConfig extends AbstractConfig {

    static final String PARTITION_NAMES = "http.partitions";
    private static final String PARTITION_NAMESPACE = "http.partitions.%s";
    private static final String HTTP_NAMESPACE = "http";

    private final Map<Partition, HttpSourceTaskPartition> partitions;
    private final ManagedThrottler throttler;

    HttpSourceTaskConfig(Map<String, ?> originals) {
        super(config(), originals);

        partitions = breakDownList(getString(PARTITION_NAMES)).stream()
                .map(partitionName -> createTaskPartition(originals, partitionName))
                .collect(toMap(HttpSourceTaskPartition::getPartition, identity()));

        throttler = new TimerThrottler(new CompositeTimer(partitions.values().stream()
                .map(partition -> (Timer) partition::getRemainingMillis)
                .collect(toList())));
    }

    private static HttpSourceTaskPartition createTaskPartition(Map<String, ?> originals, String partitionName) {
        HttpSourceTaskPartitionConfig config = new HttpSourceTaskPartitionConfig(partitionName, getPartitionOverrides(partitionName, originals));
        HttpSourceTaskPartition taskPartition = new HttpSourceTaskPartition(__ -> config);
        taskPartition.configure(emptyMap());
        return taskPartition;
    }

    private static Map<String, ?> getPartitionOverrides(String name, Map<String, ?> originals) {
        return replaceKey(String.format(PARTITION_NAMESPACE, name), HTTP_NAMESPACE, originals);
    }

    public static ConfigDef config() {
        ConfigDef config = new ConfigDef()
                .define(PARTITION_NAMES, STRING, "", LOW, "Task Partition Names");
        config.embed("", "", 0, HttpSourceTaskPartitionConfig.config());
        return config;
    }
}
