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

import lombok.Getter;
import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.common.config.ConfigDef;

import java.util.Map;

import static org.apache.kafka.common.config.ConfigDef.Importance.HIGH;
import static org.apache.kafka.common.config.ConfigDef.Importance.LOW;
import static org.apache.kafka.common.config.ConfigDef.Type.STRING;

@Getter
public class SimpleKvSourceRecordMapperConfig extends AbstractConfig {

    private static final String TOPIC = "kafka.topic";
    private static final String KEY_PROPERTY_NAME = "http.record.schema.key.property.name";
    private static final String VALUE_PROPERTY_NAME = "http.record.schema.value.property.name";

    private final String topic;
    private final String keyPropertyName;
    private final String valuePropertyName;

    SimpleKvSourceRecordMapperConfig(Map<String, ?> originals) {
        super(config(), originals);
        topic = getString(TOPIC);
        keyPropertyName = getString(KEY_PROPERTY_NAME);
        valuePropertyName = getString(VALUE_PROPERTY_NAME);
    }

    public static ConfigDef config() {
        return new ConfigDef()
                .define(TOPIC, STRING, HIGH, "Kafka Topic")
                .define(KEY_PROPERTY_NAME, STRING, "key", LOW, "Key property name in KVSchema")
                .define(VALUE_PROPERTY_NAME, STRING, "value", LOW, "Value property name in KVSchema");
    }
}
