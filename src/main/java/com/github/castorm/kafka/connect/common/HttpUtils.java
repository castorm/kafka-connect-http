package com.github.castorm.kafka.connect.common;

import lombok.experimental.UtilityClass;

import java.util.AbstractMap.SimpleEntry;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;

@UtilityClass
public class HttpUtils {

    public static Map<String, List<String>> breakDownHeaders(String headers) {
        return breakDownPairs(headers, ",", ":");
    }

    public static Map<String, List<String>> breakDownQueryParams(String queryParams) {
        return breakDownPairs(queryParams, "&", "=");
    }

    private static Map<String, List<String>> breakDownPairs(String itemLine, String itemSplitter, String pairSplitter) {
        if (itemLine == null || itemLine.length() == 0) {
            return emptyMap();
        }
        return Stream.of(itemLine.split(itemSplitter))
                .map(headerLine -> toPair(headerLine, pairSplitter))
                .collect(groupingBy(Entry::getKey, mapping(Entry::getValue, toList())));
    }

    private static Entry<String, String> toPair(String pairLine, String pairSplitter) {
        String[] parts = pairLine.split(pairSplitter, 2);
        if (parts.length < 2) {
            throw new IllegalStateException("Incomplete pair: " + pairLine);
        }
        return new SimpleEntry<>(parts[0].trim(), parts[1].trim());
    }
}
