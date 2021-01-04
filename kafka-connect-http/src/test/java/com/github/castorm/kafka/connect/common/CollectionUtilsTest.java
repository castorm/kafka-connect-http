package com.github.castorm.kafka.connect.common;

/*-
 * #%L
 * Kafka Connect HTTP
 * %%
 * Copyright (C) 2020 - 2021 Cástor Rodríguez
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

import static com.github.castorm.kafka.connect.common.CollectionUtils.merge;
import static com.github.castorm.kafka.connect.common.CollectionUtilsTest.Fixture.map1;
import static com.github.castorm.kafka.connect.common.CollectionUtilsTest.Fixture.map1and3;
import static com.github.castorm.kafka.connect.common.CollectionUtilsTest.Fixture.map2;
import static com.github.castorm.kafka.connect.common.CollectionUtilsTest.Fixture.map3;
import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;

class CollectionUtilsTest {

    @Test
    void whenMergeMapWithEmpty_thenMap() {
        assertThat(merge(map1, emptyMap())).isEqualTo(map1);
    }

    @Test
    void whenMergeEmptyWithMap_thenMap() {
        assertThat(merge(emptyMap(), map1)).isEqualTo(map1);
    }

    @Test
    void whenMergeMapSameKey_thenLastValueWins() {
        assertThat(merge(map1, map2)).isEqualTo(map2);
    }

    @Test
    void whenMergeMapDiffKey_thenCombined() {
        assertThat(merge(map1, map3)).isEqualTo(map1and3);
    }

    interface Fixture {
        ImmutableMap<String, String> map1 = ImmutableMap.of("k1", "v1");
        ImmutableMap<String, String> map2 = ImmutableMap.of("k1", "v2");
        ImmutableMap<String, String> map3 = ImmutableMap.of("k2", "v3");
        ImmutableMap<String, String> map1and3 = ImmutableMap.of("k1", "v1", "k2", "v3");
    }
}
