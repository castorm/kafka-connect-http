package com.github.castorm.kafka.connect.common;

/*-
 * #%L
 * kafka-connect-http-plugin
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

import java.util.AbstractMap.SimpleEntry;

import static com.github.castorm.kafka.connect.common.MapUtils.breakDownHeaders;
import static com.github.castorm.kafka.connect.common.MapUtils.breakDownMap;
import static com.github.castorm.kafka.connect.common.MapUtils.breakDownQueryParams;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

class MapUtilsTest {

    @Test
    void whenBreakDownNullHeaders_thenEmptyMap() {
        assertThat(breakDownHeaders(null)).isEmpty();
    }

    @Test
    void whenBreakDownEmptyStringHeaders_thenEmptyMap() {
        assertThat(breakDownHeaders("")).isEmpty();
    }

    @Test
    void whenBreakDownIncompleteHeaders_thenIllegalState() {
        assertThat(catchThrowable(() -> breakDownHeaders("Name"))).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void whenBreakDownHeaders_thenBrokenDown() {
        assertThat(breakDownHeaders("Name:Value")).containsExactly(new SimpleEntry<>("Name", singletonList("Value")));
    }

    @Test
    void whenBreakDownHeadersWithSpaces_thenBrokenDown() {
        assertThat(breakDownHeaders(" Name : Value ")).containsExactly(new SimpleEntry<>("Name", singletonList("Value")));
    }

    @Test
    void whenBreakDownTwoHeaders_thenBrokenDown() {
        assertThat(breakDownHeaders("Name1:Value1,Name2:Value2"))
                .contains(new SimpleEntry<>("Name1", singletonList("Value1")), new SimpleEntry<>("Name2", singletonList("Value2")));
    }

    @Test
    void whenBreakDownMultiValueHeaders_thenBrokenDown() {
        assertThat(breakDownHeaders("Name1:Value1,Name1:Value2")).containsExactly(new SimpleEntry<>("Name1", asList("Value1", "Value2")));
    }

    @Test
    void whenBreakDownNullQueryParams_thenEmptyMap() {
        assertThat(breakDownQueryParams(null)).isEmpty();
    }

    @Test
    void whenBreakDownEmptyStringQueryParams_thenEmptyMap() {
        assertThat(breakDownQueryParams("")).isEmpty();
    }

    @Test
    void whenBreakDownIncompleteQueryParams_thenIllegalState() {
        assertThat(catchThrowable(() -> breakDownQueryParams("name"))).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void whenBreakDownQueryParams_thenBrokenDown() {
        assertThat(breakDownQueryParams("name=value")).containsExactly(new SimpleEntry<>("name", singletonList("value")));
    }

    @Test
    void whenBreakDownQueryParamsWithSpaces_thenBrokenDown() {
        assertThat(breakDownQueryParams("  name  =  value  ")).containsExactly(new SimpleEntry<>("name", singletonList("value")));
    }

    @Test
    void whenBreakDownTwoQueryParams_thenBrokenDown() {
        assertThat(breakDownQueryParams("name1=value1&name2=value2"))
                .contains(new SimpleEntry<>("name1", singletonList("value1")), new SimpleEntry<>("name2", singletonList("value2")));
    }

    @Test
    void whenBreakDownNullMap_thenEmptyMap() {
        assertThat(breakDownMap(null)).isEmpty();
    }

    @Test
    void whenBreakDownEmptyStringMap_thenEmptyMap() {
        assertThat(breakDownMap("")).isEmpty();
    }

    @Test
    void whenBreakDownIncompleteMap_thenIllegalState() {
        assertThat(catchThrowable(() -> breakDownMap("name"))).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void whenBreakDownMap_thenBrokenDown() {
        assertThat(breakDownMap("name=value")).containsExactly(new SimpleEntry<>("name", "value"));
    }

    @Test
    void whenBreakDownMapWithSpaces_thenBrokenDown() {
        assertThat(breakDownMap("  name  =  value  ")).containsExactly(new SimpleEntry<>("name", "value"));
    }

    @Test
    void whenBreakDownTwoFoldMap_thenBrokenDown() {
        assertThat(breakDownMap("name1=value1,name2=value2"))
                .contains(new SimpleEntry<>("name1", "value1"), new SimpleEntry<>("name2", "value2"));
    }
}
