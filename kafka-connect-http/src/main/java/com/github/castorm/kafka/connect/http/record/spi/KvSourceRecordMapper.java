package com.github.castorm.kafka.connect.http.record.spi;

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

import com.github.castorm.kafka.connect.http.record.model.KvRecord;
import org.apache.kafka.common.Configurable;
import org.apache.kafka.connect.source.SourceRecord;

import java.util.Map;

@FunctionalInterface
public interface KvSourceRecordMapper extends Configurable {

    SourceRecord map(String endpoint, KvRecord record);

    default void configure(Map<String, ?> map) {
        // Do nothing
    }
}
