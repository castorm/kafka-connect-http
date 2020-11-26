package com.github.castorm.kafka.connect.http;

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

import lombok.extern.slf4j.Slf4j;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static com.github.castorm.kafka.connect.common.CollectorsUtils.toLinkedHashMap;

@Slf4j
public class ConfirmationWindow<T> {

    private final LinkedHashMap<T, Boolean> confirmedOffsets;

    public ConfirmationWindow(List<T> offsets) {
        confirmedOffsets = offsets.stream()
            .collect(toLinkedHashMap(Function.identity(), __ -> false));
    }

    public void confirm(T offset) {
        confirmedOffsets.replace(offset, true);

        log.debug("Confirmed offset {}", offset);
    }

    public T getLowWatermarkOffset() {
        T offset = null;
        for (Map.Entry<T, Boolean> offsetEntry : confirmedOffsets.entrySet()) {
            final Boolean offsetWasConfirmed = offsetEntry.getValue();
            final T sourceOffset = offsetEntry.getKey();
            if (offsetWasConfirmed) {
                offset = sourceOffset;
            } else {
                log.warn("Found unconfirmed offset {}. Will resume polling from previous offset. " +
                    "This might result in a number of duplicated records.", sourceOffset);

                break;
            }
        }

        return offset;
    }
}
