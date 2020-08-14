package com.github.castorm.kafka.connect.common;

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

import org.junit.jupiter.api.Test;

import static com.github.castorm.kafka.connect.common.CollectionUtils.concat;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;

class CollectionUtilsTest {

    @Test
    void givenTwoEmptyLists_whenConcat_thenEmpty() {
        assertThat(concat(emptyList(), emptyList())).isEmpty();
    }

    @Test
    void givenLeftNonEmptyList_whenConcat_thenList() {
        assertThat(concat(asList(1, 2), emptyList())).containsExactly(1, 2);
    }

    @Test
    void givenRightNonEmptyList_whenConcat_thenList() {
        assertThat(concat(emptyList(), asList(1, 2))).containsExactly(1, 2);
    }

    @Test
    void givenTwoLists_whenConcat_thenConcatenated() {
        assertThat(concat(asList(1, 2), asList(3, 4))).containsExactly(1, 2, 3, 4);
    }
}
