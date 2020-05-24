package com.github.castorm.kafka.connect.http.record;

/*-
 * #%L
 * Kafka Connect HTTP Plugin
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

import com.github.castorm.kafka.connect.http.model.Offset;
import com.github.castorm.kafka.connect.http.record.spi.SourceRecordFilterFactory;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.connect.source.SourceRecord;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

@RequiredArgsConstructor
public class OffsetRecordFilterFactory implements SourceRecordFilterFactory {

    private final SourceRecordFilterFactory delegate;

    public OffsetRecordFilterFactory() {
        this(new OffsetTimestampRecordFilterFactory());
    }

    @Override
    public Predicate<SourceRecord> create(Offset offset) {
        AtomicBoolean lastSeenReached = new AtomicBoolean(false);
        return delegate.create(offset).or(record -> {
            boolean result = lastSeenReached.get();
            if (!result && Offset.of(record.sourceOffset()).equals(offset)) {
                lastSeenReached.set(true);
            }
            return result;
        });
    }
}
