package com.github.castorm.kafka.connect.http.record;

/*-
 * #%L
 * kafka-connect-http-plugin
 * %%
 * Copyright (C) 2020 CastorM
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import lombok.Getter;
import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.common.config.ConfigDef;

import java.util.Map;

import static org.apache.kafka.common.config.ConfigDef.Importance.HIGH;
import static org.apache.kafka.common.config.ConfigDef.Type.STRING;

@Getter
public class SchemedSourceRecordMapperConfig extends AbstractConfig {

    private static final String TOPIC = "kafka.topic";

    private final String topic;

    SchemedSourceRecordMapperConfig(Map<String, ?> originals) {
        super(config(), originals);
        topic = getString(TOPIC);
    }

    public static ConfigDef config() {
        return new ConfigDef()
                .define(TOPIC, STRING, HIGH, "Kafka Topic");
    }
}
