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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.stream.Stream;

import static com.github.castorm.kafka.connect.http.response.StatusCodeHttpResponsePolicyTest.Fixture.code;
import static com.github.castorm.kafka.connect.http.response.StatusCodeHttpResponsePolicyTest.Fixture.response;
import static com.github.castorm.kafka.connect.http.response.spi.HttpResponsePolicy.HttpResponseOutcome.FAIL;
import static com.github.castorm.kafka.connect.http.response.spi.HttpResponsePolicy.HttpResponseOutcome.PROCESS;
import static com.github.castorm.kafka.connect.http.response.spi.HttpResponsePolicy.HttpResponseOutcome.SKIP;
import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class StatusCodeHttpResponsePolicyTest {

    StatusCodeHttpResponsePolicy policy;

    @Mock
    StatusCodeHttpResponsePolicyConfig config;

    @BeforeEach
    void setUp() {
        policy = new StatusCodeHttpResponsePolicy(__ -> config);
    }

    @Test
    void givenCodeProcess_whenResolve_thenProcess() {

        given(config.getProcessCodes()).willReturn(Stream.of(code).collect(toSet()));
        policy.configure(emptyMap());

        assertThat(policy.resolve(response.withCode(code))).isEqualTo(PROCESS);
    }

    @Test
    void givenCodeSkip_whenResolve_thenSkip() {

        given(config.getSkipCodes()).willReturn(Stream.of(code).collect(toSet()));
        policy.configure(emptyMap());

        assertThat(policy.resolve(response.withCode(code))).isEqualTo(SKIP);
    }

    @Test
    void givenCodeNoProcessNorSkip_whenResolve_thenFail() {

        given(config.getProcessCodes()).willReturn(emptySet());
        given(config.getSkipCodes()).willReturn(emptySet());
        policy.configure(emptyMap());

        assertThat(policy.resolve(response.withCode(code))).isEqualTo(FAIL);
    }

    interface Fixture {
        HttpResponse response = HttpResponse.builder().build();
        int code = 200;
    }
}
