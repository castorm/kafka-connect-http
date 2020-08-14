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

import com.github.castorm.kafka.connect.timer.spi.ManagedThrottler;
import com.github.castorm.kafka.connect.timer.spi.Throttler;
import com.github.castorm.kafka.connect.timer.spi.Timer;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TimerThrottler implements ManagedThrottler {

    private final Timer timer;

    private final Throttler sleeper;

    public TimerThrottler(Timer timer) {
        this(timer, Thread::sleep);
    }

    @Override
    public void sleep() throws InterruptedException {
        Long remainingMillis = timer.getRemainingMillis();
        if (remainingMillis > 0)
            sleeper.sleep(remainingMillis);
    }
}
