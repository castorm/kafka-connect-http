package com.github.castorm.kafka.connect.http.poll;

import lombok.Getter;
import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.common.config.ConfigDef;

import java.util.Map;

import static org.apache.kafka.common.config.ConfigDef.Importance.HIGH;
import static org.apache.kafka.common.config.ConfigDef.Type.LONG;

@Getter
public class IntervalDelayPollInterceptorConfig extends AbstractConfig {

    private static final String POLL_INTERVAL_MILLIS = "http.source.poll.interval.millis";

    private final Long pollIntervalMillis;

    public IntervalDelayPollInterceptorConfig(Map<String, ?> originals) {
        super(config(), originals);
        pollIntervalMillis = getLong(POLL_INTERVAL_MILLIS);
    }

    public static ConfigDef config() {
        return new ConfigDef()
                .define(POLL_INTERVAL_MILLIS, LONG, 60000L, HIGH, "Poll Interval Millis");
    }
}
