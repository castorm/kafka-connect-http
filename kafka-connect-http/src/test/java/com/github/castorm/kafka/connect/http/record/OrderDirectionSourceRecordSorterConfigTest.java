package com.github.castorm.kafka.connect.http.record;

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

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static com.github.castorm.kafka.connect.http.record.OrderDirectionSourceRecordSorter.OrderDirection.ASC;
import static com.github.castorm.kafka.connect.http.record.OrderDirectionSourceRecordSorter.OrderDirection.IMPLICIT;
import static com.github.castorm.kafka.connect.http.record.OrderDirectionSourceRecordSorterConfigTest.Fixture.config;
import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;

class OrderDirectionSourceRecordSorterConfigTest {

    @Test
    void whenNoDirection_thenDefault() {
        assertThat(config(emptyMap()).getOrderDirection()).isEqualTo(IMPLICIT);
    }

    @Test
    void whenDirection_thenInitialized() {
        assertThat(config(ImmutableMap.of("http.response.list.order.direction", "ASC")).getOrderDirection()).isEqualTo(ASC);
    }

    interface Fixture {
        static OrderDirectionSourceRecordSorterConfig config(Map<String, String> settings) {
            return new OrderDirectionSourceRecordSorterConfig(settings);
        }
    }

}
