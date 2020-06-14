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

import com.github.castorm.kafka.connect.http.model.Partition;
import com.github.castorm.kafka.connect.http.partition.spi.PartitionProvider;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static com.github.castorm.kafka.connect.http.HttpSourceTaskConfigTest.Fixture.*;
import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;

class HttpSourceTaskConfigTest {

    @Test
    void whenNoPartitionProviderAndNoPartition_thenDefault() {
        assertThat(configWithout("http.partitions.provider").getPartitions()).isEmpty();
    }

    @Test
    void whenTestPartitionProvider_thenPartitionKey() {
        assertThat(config(ImmutableMap.of("http.partitions.provider", TestPartitionProvider.class.getName())).getPartitions()).containsKeys(partition);
    }

    public static class TestPartitionProvider implements PartitionProvider {
        public Collection<Partition> getPartitions() { return ImmutableList.of(partition); }
    }

    interface Fixture {
        Partition partition = Partition.of("test", emptyMap());

        static Map<String, String> defaultMap() {
            return new HashMap<String, String>() {{
                put("kafka.topic", "topic");
                put("http.request.url", "foo");
                put("http.response.json.record.offset.value.pointer", "/baz");
            }};
        }

        static HttpSourceTaskConfig config(Map<String, String> extra) {
            Map<String, String> customMap = new HashMap<>(defaultMap());
            customMap.putAll(extra);
            return new HttpSourceTaskConfig(customMap);
        }

        static HttpSourceTaskConfig configWithout(String key) {
            Map<String, String> customMap = defaultMap();
            customMap.remove(key);
            return new HttpSourceTaskConfig(customMap);
        }
    }
}
