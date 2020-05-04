package com.github.castorm.kafka.connect.http.poll;

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
    public void afterPoll(List<SourceRecord> records) {
        upToDate = records.isEmpty();
    }

    @FunctionalInterface
    public interface Sleeper {

        void sleep(Long milliseconds) throws InterruptedException;
    }
}
