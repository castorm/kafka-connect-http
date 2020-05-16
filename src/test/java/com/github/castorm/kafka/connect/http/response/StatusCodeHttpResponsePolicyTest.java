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
import org.junit.jupiter.api.Test;

import static com.github.castorm.kafka.connect.http.response.StatusCodeHttpResponsePolicyTest.Fixture.response;
import static com.github.castorm.kafka.connect.http.response.spi.HttpResponsePolicy.HttpResponseOutcome.FAIL;
import static com.github.castorm.kafka.connect.http.response.spi.HttpResponsePolicy.HttpResponseOutcome.PROCESS;
import static com.github.castorm.kafka.connect.http.response.spi.HttpResponsePolicy.HttpResponseOutcome.SKIP;
import static org.assertj.core.api.Assertions.assertThat;

class StatusCodeHttpResponsePolicyTest {

    StatusCodeHttpResponsePolicy policy = new StatusCodeHttpResponsePolicy();

    @Test
    void givenCode199_whenResolve_thenFail() {
        assertThat(policy.resolve(response.withCode(199))).isEqualTo(FAIL);
    }

    @Test
    void givenCode200_whenResolve_thenProcess() {
        assertThat(policy.resolve(response.withCode(200))).isEqualTo(PROCESS);
    }

    @Test
    void givenCode299_whenResolve_thenDelegate() {
        assertThat(policy.resolve(response.withCode(299))).isEqualTo(PROCESS);
    }

    @Test
    void givenCode300_whenResolve_thenSkip() {
        assertThat(policy.resolve(response.withCode(300))).isEqualTo(SKIP);
    }

    @Test
    void givenCode400_whenResolve_thenFail() {
        assertThat(policy.resolve(response.withCode(400))).isEqualTo(FAIL);
    }

    @Test
    void givenCode500_whenResolve_thenFail() {
        assertThat(policy.resolve(response.withCode(400))).isEqualTo(FAIL);
    }

    interface Fixture {
        HttpResponse response = HttpResponse.builder().build();
    }
}
