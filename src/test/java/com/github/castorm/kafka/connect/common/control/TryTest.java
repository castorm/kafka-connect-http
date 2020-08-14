package com.github.castorm.kafka.connect.common.control;

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

import org.junit.jupiter.api.Test;

import java.util.concurrent.Callable;

import static com.github.castorm.kafka.connect.common.control.TryTest.Fixture.cause;
import static com.github.castorm.kafka.connect.common.control.TryTest.Fixture.value;
import static com.github.castorm.kafka.connect.common.control.TryTest.Fixture.willFail;
import static com.github.castorm.kafka.connect.common.control.TryTest.Fixture.willSucceed;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

class TryTest {

    @Test
    void givenSuccess_whenCreate_thenSuccess() {
        assertThat(Try.of(willSucceed)).isInstanceOf(Try.Success.class);
    }

    @Test
    void givenSuccess_whenGetValue_thenValue() {
        assertThat(Try.of(willSucceed).getValue()).isEqualTo(value);
    }

    @Test
    void givenSuccess_whenGetCause_thenUnsupported() {
        assertThat(catchThrowable(() -> Try.of(willSucceed).getCause())).isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void givenSuccess_whenGetOrFail_thenValue() {
        assertThat(Try.of(willSucceed).getOrFail()).isEqualTo(value);
    }

    @Test
    void givenFailure_whenCreate_thenFailure() {
        assertThat(Try.of(willFail)).isInstanceOf(Try.Failure.class);
    }

    @Test
    void givenFailure_whenGetCause_thenException() {
        assertThat(Try.of(willFail).getCause()).isEqualTo(cause);
    }

    @Test
    void givenFailure_whenGetValue_thenUnsupported() {
        assertThat(catchThrowable(() -> Try.of(willFail).getValue())).isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void givenFailure_whenGetOrFail_thenValue() {
        assertThat(catchThrowable(() -> Try.of(willFail).getOrFail())).hasCauseInstanceOf(cause.getClass());
    }

    @Test
    void givenTwoSucceeds_whenZip_thenReduced() {
        assertThat(Try.zip((a, b) -> a + "+" + b).apply(Try.of(() -> "1"), Try.of(() -> "2")).getOrFail()).isEqualTo("1+2");
    }

    @Test
    void givenLeftFail_whenZip_thenException() {
        assertThat(catchThrowable(() -> Try.zip((a, b) -> a + "+" + b).apply(Try.of(willFail), Try.of(() -> "2")).getOrFail())).hasCauseInstanceOf(IllegalStateException.class);
    }

    @Test
    void givenRightFail_whenZip_thenException() {
        assertThat(catchThrowable(() -> Try.zip((a, b) -> a + "+" + b).apply(Try.of(() -> "1"), Try.of(willFail)).getOrFail())).hasCauseInstanceOf(IllegalStateException.class);
    }

    @Test
    void givenBothFail_whenZip_thenException() {
        assertThat(catchThrowable(() -> Try.zip((a, b) -> a + "+" + b).apply(Try.of(willFail), Try.of(willFail)).getOrFail())).hasCauseInstanceOf(IllegalStateException.class);
    }

    interface Fixture {
        int value = 42;
        Callable<Integer> willSucceed = () -> value;
        IllegalStateException cause = new IllegalStateException("failed!");
        Callable<Object> willFail = () -> {
            throw cause;
        };
    }
}
