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

import com.github.castorm.kafka.connect.timer.spi.Sleeper;
import com.github.castorm.kafka.connect.timer.spi.Timer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class TimerThrottlerTest {

    @InjectMocks
    TimerThrottler sleeper;

    @Mock
    Timer timer;

    @Mock
    Sleeper externalSleeper;

    @Test
    void givenTimerZero_whenSleep_thenDoNotSleep() throws InterruptedException {

        given(timer.getRemainingMillis()).willReturn(0L);

        sleeper.throttle();

        then(externalSleeper).should(never()).sleep(any());
    }

    @Test
    void givenTimer_whenSleep_thenSleepForTime() throws InterruptedException {

        given(timer.getRemainingMillis()).willReturn(42L);

        sleeper.throttle();

        then(externalSleeper).should().sleep(42L);
    }
}
