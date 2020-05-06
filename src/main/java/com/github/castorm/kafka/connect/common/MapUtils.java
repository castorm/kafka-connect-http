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

import lombok.experimental.UtilityClass;

import java.util.AbstractMap.SimpleEntry;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collector;
import java.util.stream.Stream;

import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

@UtilityClass
public class MapUtils {

    public static Map<String, List<String>> breakDownHeaders(String headers) {
        return breakDownMultiValuePairs(headers, ",", ":");
    }

    public static Map<String, List<String>> breakDownQueryParams(String queryParams) {
        return breakDownMultiValuePairs(queryParams, "&", "=");
    }

    public static Map<String, String> breakDownMap(String mapString) {
        return breakDownPairs(mapString, ",", "=");
    }

    private static Map<String, String> breakDownPairs(String itemLine, String itemSplitter, String pairSplitter) {
        return breakDownPairs(itemLine, itemSplitter, pairSplitter, toMap(Entry::getKey, Entry::getValue));
    }

    private static Map<String, List<String>> breakDownMultiValuePairs(String itemLine, String itemSplitter, String pairSplitter) {
        return breakDownPairs(itemLine, itemSplitter, pairSplitter, groupingBy(Entry::getKey, mapping(Entry::getValue, toList())));
    }

    private static <T> Map<String, T> breakDownPairs(String itemLine, String itemSplitter, String pairSplitter, Collector<Entry<String, String>, ?, Map<String, T>> collector) {
        if (itemLine == null || itemLine.length() == 0) {
            return emptyMap();
        }
        return Stream.of(itemLine.split(itemSplitter))
                .map(headerLine -> toPair(headerLine, pairSplitter))
                .collect(collector);
    }

    private static Entry<String, String> toPair(String pairLine, String pairSplitter) {
        String[] parts = pairLine.split(pairSplitter, 2);
        if (parts.length < 2) {
            throw new IllegalStateException("Incomplete pair: " + pairLine);
        }
        return new SimpleEntry<>(parts[0].trim(), parts[1].trim());
    }
}
