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
import com.github.castorm.kafka.connect.http.response.spi.HttpResponseParser;
import com.github.castorm.kafka.connect.http.response.spi.HttpResponsePolicy;
import com.google.common.collect.ImmutableList;
import org.apache.kafka.connect.source.SourceRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.github.castorm.kafka.connect.http.response.PolicyHttpResponseParserTest.Fixture.record;
import static com.github.castorm.kafka.connect.http.response.PolicyHttpResponseParserTest.Fixture.response;
import static com.github.castorm.kafka.connect.http.response.spi.HttpResponsePolicy.HttpResponseOutcome.FAIL;
import static com.github.castorm.kafka.connect.http.response.spi.HttpResponsePolicy.HttpResponseOutcome.PROCESS;
import static com.github.castorm.kafka.connect.http.response.spi.HttpResponsePolicy.HttpResponseOutcome.SKIP;
import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class PolicyHttpResponseParserTest {

    PolicyHttpResponseParser parser;

    @Mock
    PolicyHttpResponseParserConfig config;

    @Mock
    HttpResponseParser delegate;

    @Mock
    HttpResponsePolicy policy;

    @BeforeEach
    void setUp() {
        parser = new PolicyHttpResponseParser(__ -> config);
        given(config.getDelegateParser()).willReturn(delegate);
        given(config.getPolicy()).willReturn(policy);
        parser.configure(emptyMap());
    }

    @Test
    void givenPolicyFail_whenParse_thenIllegalState() {

        given(policy.resolve(response)).willReturn(FAIL);

        assertThat(catchThrowable(() -> parser.parse("dummy-endpoint", response))).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void givenPolicyFail_whenParse_thenDontDelegate() {

        given(policy.resolve(response)).willReturn(FAIL);

        catchThrowable(() -> parser.parse("dummy-endpoint", response));

        then(delegate).should(never()).parse(any(), any());
    }

    @Test
    void givenPolicyProcess_whenParse_thenDelegate() {

        given(policy.resolve(response)).willReturn(PROCESS);

        parser.parse("dummy-endpoint", response);

        then(delegate).should().parse("dummy-endpoint", response);
    }

    @Test
    void givenPolicyProcess_whenParse_thenResponseFromDelegate() {

        given(policy.resolve(response)).willReturn(PROCESS);

        given(delegate.parse("dummy-endpoint", response)).willReturn(ImmutableList.of(record));

        assertThat(parser.parse("dummy-endpoint", response)).containsExactly(record);
    }

    @Test
    void givenPolicySkip_whenParse_thenDontDelegate() {

        given(policy.resolve(response)).willReturn(SKIP);

        parser.parse("dummy-endpoint", response);

        then(delegate).should(never()).parse(any(), any());
    }

    @Test
    void givenPolicySkip_whenParse_thenEmptyList() {

        given(policy.resolve(response)).willReturn(SKIP);

        assertThat(parser.parse("dummy-endpoint", response)).isEmpty();
    }

    interface Fixture {
        HttpResponse response = HttpResponse.builder().build();
        SourceRecord record = new SourceRecord(null, null, null, null, "Something");
    }
}
