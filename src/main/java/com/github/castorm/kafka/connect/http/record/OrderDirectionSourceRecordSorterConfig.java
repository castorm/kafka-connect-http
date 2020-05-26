package com.github.castorm.kafka.connect.http.record;

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

import com.github.castorm.kafka.connect.http.record.OrderDirectionSourceRecordSorter.OrderDirection;
import lombok.Getter;
import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.common.config.ConfigDef;

import java.util.Map;

import static org.apache.kafka.common.config.ConfigDef.Importance.LOW;
import static org.apache.kafka.common.config.ConfigDef.Type.STRING;

@Getter
public class OrderDirectionSourceRecordSorterConfig extends AbstractConfig {

    private static final String ORDER_DIRECTION = "http.response.list.order.direction";

    private final OrderDirection orderDirection;

    OrderDirectionSourceRecordSorterConfig(Map<String, ?> originals) {
        super(config(), originals);
        orderDirection = OrderDirection.valueOf(getString(ORDER_DIRECTION).toUpperCase());
    }

    public static ConfigDef config() {
        return new ConfigDef()
                .define(ORDER_DIRECTION, STRING, "IMPLICIT", LOW, "Order direction of the results in the list, either ASC, DESC or IMPLICIT");
    }
}
