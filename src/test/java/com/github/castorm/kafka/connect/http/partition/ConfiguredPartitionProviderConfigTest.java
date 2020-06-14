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
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;

class ConfiguredPartitionProviderConfigTest {

    @Test
    void whenNoPartition_thenEmpty() {
        assertThat(config(emptyMap()).getPartitions()).isEmpty();
    }

    @Test
    void whenOnePartition_thenOnePartition() {
        assertThat(config(ImmutableMap.of("http.partitions", "a")).getPartitions()).containsExactly(Partition.of("a", emptyMap()));
    }

    @Test
    void whenTwoPartitions_thenTwoPartitions() {
        assertThat(config(ImmutableMap.of("http.partitions", "a, b")).getPartitions()).containsExactly(Partition.of("a", emptyMap()), Partition.of("b", emptyMap()));
    }

    @Test
    void whenOnePartitionWithProperties_thenOnePartitionWithProperties() {
        assertThat(config(ImmutableMap.of("http.partitions", "a", "http.partitions.a.properties", "x=1,y=2")).getPartitions()).containsExactly(Partition.of("a", ImmutableMap.of("x", "1", "y", "2")));
    }

    private static ConfiguredPartitionProviderConfig config(Map<String, String> config) {
        return new ConfiguredPartitionProviderConfig(config);
    }
}
