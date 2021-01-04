package com.github.castorm.kafka.connect.http.response.jackson.model;

/*-
 * #%L
 * Kafka Connect HTTP
 * %%
 * Copyright (C) 2020 - 2021 Cástor Rodríguez
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

import lombok.Builder;
import lombok.Builder.Default;
import lombok.Value;
import lombok.With;

import java.util.Map;

import static java.util.Collections.emptyMap;

@With
@Value
@Builder
public class JacksonRecord {

    /**
     * @deprecated To be integrated in offset
     */
    @Deprecated
    String key;

    /**
     * @deprecated To be integrated in offset
     */
    @Deprecated
    String timestamp;

    @Default
    Map<String, Object> offset = emptyMap();

    String body;
}
