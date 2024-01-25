package com.github.castorm.kafka.connect.http.model;

import java.util.Map;

public class Partition {
    public static Map<String, ?> getPartition(String endpoint) {
         return Map.of("endpoint", endpoint);
    }

    public static String getEndpointFromPartition(Map<String, ?> partition) {
        return partition.get("endpoint").toString();
    }

}
