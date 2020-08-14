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

import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.connect.connector.Task;
import org.apache.kafka.connect.source.SourceConnector;

import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static com.github.castorm.kafka.connect.common.ConfigUtils.breakDownList;
import static com.github.castorm.kafka.connect.common.VersionUtils.getVersion;
import static com.github.castorm.kafka.connect.http.HttpSourceTaskConfig.PARTITION_NAMES;
import static java.lang.Math.min;
import static java.lang.String.join;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.IntStream.range;

public class HttpSourceConnector extends SourceConnector {

    private Map<String, String> settings = emptyMap();

    private List<String> partitionNames = emptyList();

    @Override
    public void start(Map<String, String> settings) {
        this.settings = settings;
        partitionNames = breakDownList(settings.getOrDefault(PARTITION_NAMES, ""));
    }

    @Override
    public void stop() {
        settings = emptyMap();
    }

    @Override
    public ConfigDef config() {
        return HttpSourceConnectorConfig.config();
    }

    @Override
    public Class<? extends Task> taskClass() {
        return HttpSourceTask.class;
    }

    @Override
    public List<Map<String, String>> taskConfigs(int taskCount) {
        return partitionNames.isEmpty() ? getTaskConfigs(settings, taskCount) : getTaskConfigsPartitioned(settings, taskCount, partitionNames);
    }

    private static List<Map<String, String>> getTaskConfigs(Map<String, String> settings, int taskCount) {
        return range(0, taskCount).boxed().map(__ -> settings).collect(toList());
    }

    private static List<Map<String, String>> getTaskConfigsPartitioned(Map<String, String> settings, int taskCount, List<String> partitionNames) {
        int maxTaskCount = min(taskCount, partitionNames.size());
        Map<Integer, List<String>> partitionNamesByTask = getPartitionNamesByTask(maxTaskCount, partitionNames);
        return range(0, maxTaskCount).boxed()
                .map(partitionNamesByTask::get)
                .map(names -> createTaskSettings(settings, names))
                .collect(toList());
    }

    private static Map<Integer, List<String>> getPartitionNamesByTask(int taskCount, List<String> partitionNames) {
        return getPartitionIndexesByTask(taskCount, partitionNames.size()).entrySet().stream()
                .map(entry -> new SimpleEntry<>(entry.getKey(), entry.getValue().stream().map(partitionNames::get).collect(toList())))
                .collect(toMap(Entry::getKey, Entry::getValue));
    }

    private static Map<Integer, List<Integer>> getPartitionIndexesByTask(int maxTaskCount, int partitionCount) {
        return range(0, partitionCount).boxed()
                .collect(groupingBy(i -> i % maxTaskCount, toList()));
    }

    private static Map<String, String> createTaskSettings(Map<String, String> settings, List<String> partitionNames) {
        Map<String, String> partitionSettings = new HashMap<>(settings);
        partitionSettings.put(PARTITION_NAMES, join(",", partitionNames));
        return partitionSettings;
    }

    @Override
    public String version() {
        return getVersion();
    }
}
