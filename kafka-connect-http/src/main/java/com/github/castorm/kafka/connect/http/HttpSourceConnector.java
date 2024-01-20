package com.github.castorm.kafka.connect.http;

import org.apache.commons.collections4.ListUtils;

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
import org.apache.kafka.common.config.ConfigException;
import org.apache.kafka.connect.connector.Task;
import org.apache.kafka.connect.source.SourceConnector;

import java.util.List;
import java.util.Map;

import static com.github.castorm.kafka.connect.common.VersionUtils.getVersion;
import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;

import java.util.ArrayList;
import java.util.HashMap;

public class HttpSourceConnector extends SourceConnector {

    private Map<String, String> settings;

    @Override
    public void start(Map<String, String> settings) {
        this.settings = settings;
    }

    @Override
    public void stop() {
        settings = null;
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
    public List<Map<String, String>> taskConfigs(int maxTasks) {
        List<Map<String, String>> taskConfigs = new ArrayList<>();

        String indexIncludeList = settings.get(HttpSourceConnectorConfig.INDEX_INCLUDE_LIST);
        if (null == indexIncludeList) {
            throw new ConfigException(HttpSourceConnectorConfig.INDEX_INCLUDE_LIST + " is required");
        }
        List<String> indexes = List.of(indexIncludeList.split(","));
        List<List<String>> tasksIndexIncludeLists = ListUtils.partition(indexes, maxTasks);

        for (int i = 0; i < maxTasks; i++) {
            Map<String, String> taskSettings = new HashMap<>();
            taskSettings.putAll(settings);
            List<String> taskIndexes = tasksIndexIncludeLists.size() <= i ? List.of() : tasksIndexIncludeLists.get(i);
            taskSettings.put(HttpSourceConnectorConfig.INDEX_INCLUDE_LIST,
                    String.join(",", taskIndexes));
            taskConfigs.add(taskSettings);
        }

        return taskConfigs;
    }

    @Override
    public String version() {
        return getVersion();
    }
}
