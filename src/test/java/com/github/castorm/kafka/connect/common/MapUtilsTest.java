package com.github.castorm.kafka.connect.common;

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
