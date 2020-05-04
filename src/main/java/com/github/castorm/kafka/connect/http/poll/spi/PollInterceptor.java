package com.github.castorm.kafka.connect.http.poll.spi;

import org.apache.kafka.common.Configurable;
import org.apache.kafka.connect.source.SourceRecord;

import java.util.List;

public interface PollInterceptor extends Configurable {

    void beforePoll() throws InterruptedException;

    void afterPoll(List<SourceRecord> records);
}
