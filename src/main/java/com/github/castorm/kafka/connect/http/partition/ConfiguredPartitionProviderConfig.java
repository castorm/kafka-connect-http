package com.github.castorm.kafka.connect.http.partition;

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
import lombok.Getter;
import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.common.config.ConfigDef;

import java.util.Collection;
import java.util.Map;

import static com.github.castorm.kafka.connect.common.ConfigUtils.breakDownList;
import static com.github.castorm.kafka.connect.common.ConfigUtils.breakDownMap;
import static java.util.stream.Collectors.toList;
import static org.apache.kafka.common.config.ConfigDef.Importance.LOW;
import static org.apache.kafka.common.config.ConfigDef.Type.STRING;

@Getter
class ConfiguredPartitionProviderConfig extends AbstractConfig {

    private static final String PARTITIONS = "http.partitions";
    private static final String PARTITION_PROPERTIES = "http.partitions.%s.properties";

    private final Collection<Partition> partitions;

    ConfiguredPartitionProviderConfig(Map<String, ?> originals) {
        super(config(), originals);

        partitions = breakDownList(getString(PARTITIONS)).stream()
                .map(name -> Partition.of(name, breakDownMap((String) originals.get(String.format(PARTITION_PROPERTIES, name)))))
                .collect(toList());
    }

    public static ConfigDef config() {
        return new ConfigDef()
                .define(PARTITIONS, STRING, "", LOW, "Task Partition Names");
    }
}
