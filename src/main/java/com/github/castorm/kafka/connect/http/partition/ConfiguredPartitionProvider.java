package com.github.castorm.kafka.connect.http.partition;

/*-
 * #%L
 * Kafka Connect HTTP
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
import com.github.castorm.kafka.connect.http.partition.spi.PartitionProvider;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;

@RequiredArgsConstructor
public class ConfiguredPartitionProvider implements PartitionProvider {

    private final Function<Map<String, ?>, ConfiguredPartitionProviderConfig> configFactory;

    private Collection<Partition> partitions;

    public ConfiguredPartitionProvider() {
        this(ConfiguredPartitionProviderConfig::new);
    }

    @Override
    public void configure(Map<String, ?> settings) {
        ConfiguredPartitionProviderConfig config = configFactory.apply(settings);
        partitions = config.getPartitions();
    }

    @Override
    public Collection<Partition> getPartitions() {
        return partitions;
    }
}
