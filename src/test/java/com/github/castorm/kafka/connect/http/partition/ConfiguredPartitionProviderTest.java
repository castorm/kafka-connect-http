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
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.github.castorm.kafka.connect.http.partition.ConfiguredPartitionProviderTest.Fixture.partition1;
import static com.github.castorm.kafka.connect.http.partition.ConfiguredPartitionProviderTest.Fixture.partition2;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class ConfiguredPartitionProviderTest {

    ConfiguredPartitionProvider provider;

    @Mock
    ConfiguredPartitionProviderConfig config;

    @BeforeEach
    void setUp() {
    }

    @Test
    void givenNoPartition_whenGetPartitions_thenEmpty() {

        given(config.getPartitions()).willReturn(emptyList());

        assertThat(provider().getPartitions()).isEmpty();
    }

    @Test
    void givenOnePartition_whenGetPartitions_thenConfiguredPartition() {

        given(config.getPartitions()).willReturn(ImmutableList.of(partition1));

        assertThat(provider().getPartitions()).containsExactly(partition1);
    }

    @Test
    void givenTwoPartitions_whenGetPartitions_thenConfiguredPartitions() {

        given(config.getPartitions()).willReturn(ImmutableList.of(partition1, partition2));

        assertThat(provider().getPartitions()).containsExactly(partition1, partition2);
    }

    private ConfiguredPartitionProvider provider() {
        ConfiguredPartitionProvider provider = new ConfiguredPartitionProvider(__ -> config);
        provider.configure(emptyMap());
        return provider;
    }

    interface Fixture {
        Partition partition1 = Partition.of(ImmutableMap.of("k", "v1"));
        Partition partition2 = Partition.of(ImmutableMap.of("k", "v2"));
    }
}
