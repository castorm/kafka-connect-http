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

import com.github.castorm.kafka.connect.http.record.spi.SourceRecordSorter;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.connect.source.SourceRecord;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static com.github.castorm.kafka.connect.http.record.OrderDirectionSourceRecordSorter.OrderDirection.ASC;
import static com.github.castorm.kafka.connect.http.record.OrderDirectionSourceRecordSorter.OrderDirection.DESC;
import static java.util.Collections.reverse;

@RequiredArgsConstructor
public class OrderDirectionSourceRecordSorter implements SourceRecordSorter {

    private final Function<Map<String, ?>, OrderDirectionSourceRecordSorterConfig> configFactory;

    private OrderDirection orderDirection;

    public OrderDirectionSourceRecordSorter() {
        this(OrderDirectionSourceRecordSorterConfig::new);
    }

    @Override
    public void configure(Map<String, ?> settings) {
        orderDirection = configFactory.apply(settings).getOrderDirection();
    }

    @Override
    public List<SourceRecord> sort(List<SourceRecord> records) {
        return sortWithDirection(records, orderDirection);
    }

    private static List<SourceRecord> sortWithDirection(List<SourceRecord> records, OrderDirection direction) {
        switch (direction) {
            case DESC:
                List<SourceRecord> reversed = new ArrayList<>(records);
                reverse(reversed);
                return reversed;
            case ASC:
                return records;
            case IMPLICIT:
            default:
                return sortWithDirection(records, getImplicitDirection(records));
        }
    }

    private static OrderDirection getImplicitDirection(List<SourceRecord> records) {
        if (records.size() >= 2) {
            Long first = records.get(0).timestamp();
            Long last = records.get(records.size() - 1).timestamp();
            return first <= last ? ASC : DESC;
        }
        return ASC;
    }

    public enum OrderDirection {
        ASC, DESC, IMPLICIT
    }
}
