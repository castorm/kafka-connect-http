package com.github.castorm.kafka.connect.http;

import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.connect.connector.Task;
import org.apache.kafka.connect.source.SourceConnector;

import java.util.List;
import java.util.Map;

import static com.github.castorm.kafka.connect.common.VersionUtils.getVersion;
import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;

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
        return range(0, maxTasks).boxed()
                .map(__ -> settings)
                .collect(toList());
    }

    @Override
    public String version() {
        return getVersion(getClass());
    }
}
