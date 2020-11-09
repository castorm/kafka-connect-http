package com.github.castorm.kafka.connect.http.response;

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

import com.github.castorm.kafka.connect.http.model.HttpResponse;
import com.github.castorm.kafka.connect.http.response.spi.HttpResponsePolicy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static com.github.castorm.kafka.connect.http.response.spi.HttpResponsePolicy.HttpResponseOutcome.FAIL;
import static com.github.castorm.kafka.connect.http.response.spi.HttpResponsePolicy.HttpResponseOutcome.PROCESS;
import static com.github.castorm.kafka.connect.http.response.spi.HttpResponsePolicy.HttpResponseOutcome.SKIP;

@Slf4j
@RequiredArgsConstructor
public class StatusCodeHttpResponsePolicy implements HttpResponsePolicy {

    private final Function<Map<String, ?>, StatusCodeHttpResponsePolicyConfig> configFactory;

    private Set<Integer> processCodes;

    private Set<Integer> skipCodes;

    public StatusCodeHttpResponsePolicy() {
        this(StatusCodeHttpResponsePolicyConfig::new);
    }

    @Override
    public void configure(Map<String, ?> settings) {
        StatusCodeHttpResponsePolicyConfig config = configFactory.apply(settings);
        processCodes = config.getProcessCodes();
        skipCodes = config.getSkipCodes();
    }

    @Override
    public HttpResponseOutcome resolve(HttpResponse response) {
        if (processCodes.contains(response.getCode())) {
            return PROCESS;
        } else if (skipCodes.contains(response.getCode())) {
            log.warn("Unexpected HttpResponse status code: {}, continuing with no records", response.getCode());
            return SKIP;
        } else {
            return FAIL;
        }
    }
}
