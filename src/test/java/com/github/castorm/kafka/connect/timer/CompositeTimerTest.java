package com.github.castorm.kafka.connect.timer;

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

import com.github.castorm.kafka.connect.timer.spi.Timer;
import org.junit.jupiter.api.Test;

import static com.github.castorm.kafka.connect.timer.CompositeTimerTest.Fixture.timer;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

class CompositeTimerTest {


    @Test
    void givenNoTimers_whenGetRemainingMillis_thenZero() {
        assertThat(new CompositeTimer(emptyList()).getRemainingMillis()).isEqualTo(0L);
    }

    @Test
    void givenOneTimer_whenGetRemainingMillis_thenTheOne() {
        assertThat(new CompositeTimer(singletonList(timer(1L))).getRemainingMillis()).isEqualTo(1L);
    }

    @Test
    void givenTwoTimers_whenGetRemainingMillis_thenMin() {
        assertThat(new CompositeTimer(asList(timer(1L), timer(2L))).getRemainingMillis()).isEqualTo(1L);
    }

    interface Fixture {
        static Timer timer(Long remaining) {
            return () -> remaining;
        }
    }
}
