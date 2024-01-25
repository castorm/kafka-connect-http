package com.github.castorm.kafka.connect.http.record;

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

import freemarker.template.Configuration;
import lombok.Getter;
import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.common.config.ConfigDef;

import java.util.Map;

import static org.apache.kafka.common.config.ConfigDef.Importance.HIGH;
import static org.apache.kafka.common.config.ConfigDef.Type.BOOLEAN;
import static org.apache.kafka.common.config.ConfigDef.Type.STRING;

@Getter
public class SourceRecordMapperConfig extends AbstractConfig {

    private static final String TOPIC = "kafka.topic";

    private static final String TOPIC_TEMPLATE = "kafka.topic.template";

    private final String topic;

    private final String topicTemplate;

    private final Configuration freemarkerConfig;

    SourceRecordMapperConfig(Map<String, ?> originals) {
        super(config(), originals);
        topic = getString(TOPIC);
        topicTemplate = getString(TOPIC_TEMPLATE);
        freemarkerConfig = new Configuration(Configuration.VERSION_2_3_31);
    }

    public static ConfigDef config() {
        return new ConfigDef()
                .define(TOPIC, STRING, "default-topic", HIGH, "Kafka Topic")
                .define(TOPIC_TEMPLATE, STRING, HIGH, "Kafka Topic Template");

    }

    public String getTopicName(String endpoint) {
        if (topicTemplate != null && Boolean.TRUE.toString().equals(topicTemplate.toLowerCase())) {
            if (endpoint != null && !endpoint.isEmpty()) {
                return endpoint.replaceAll("[^a-zA-Z0-9_]", "_");
            }
        }
        return topic;
    }
}
