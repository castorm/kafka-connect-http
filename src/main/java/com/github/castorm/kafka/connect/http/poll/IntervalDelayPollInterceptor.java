package com.github.castorm.kafka.connect.http.poll;

/*-
 * #%L
 * kafka-connect-http-plugin
 * %%
 * Copyright (C) 2020 CastorM
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.github.castorm.kafka.connect.http.poll.spi.PollInterceptor;
import org.apache.kafka.connect.source.SourceRecord;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static java.lang.System.currentTimeMillis;

public class IntervalDelayPollInterceptor implements PollInterceptor {

    private final Function<Map<String, ?>, IntervalDelayPollInterceptorConfig> configFactory;

    private final Sleeper sleeper;

    private Long pollIntervalMillis = 60000L;

    private Long lastPollMillis = currentTimeMillis();

    boolean upToDate = true;

    public IntervalDelayPollInterceptor() {
        this(IntervalDelayPollInterceptorConfig::new, Thread::sleep);
    }

    IntervalDelayPollInterceptor(Function<Map<String, ?>, IntervalDelayPollInterceptorConfig> configFactory, Sleeper sleeper) {
        this.configFactory = configFactory;
        this.sleeper = sleeper;
    }

    @Override
    public void configure(Map<String, ?> settings) {
        pollIntervalMillis = new IntervalDelayPollInterceptorConfig(settings).getPollIntervalMillis();
    }

    @Override
    public void beforePoll() throws InterruptedException {
        if (upToDate) {
            awaitNextTick();
        }
    }

    private void awaitNextTick() throws InterruptedException {
        long now = currentTimeMillis();
        long sinceLastPollMillis = now - lastPollMillis;
        if (pollIntervalMillis > sinceLastPollMillis) {
            sleeper.sleep(pollIntervalMillis - sinceLastPollMillis);
        }
        lastPollMillis = now;
    }

    @Override
    public List<SourceRecord> afterPoll(List<SourceRecord> records) {
        upToDate = records.isEmpty();
        return records;
    }

    @FunctionalInterface
    public interface Sleeper {

        void sleep(Long milliseconds) throws InterruptedException;
    }
}
