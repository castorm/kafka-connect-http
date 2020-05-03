package com.github.castorm.kafka.connect.http.record.spi;

import com.github.castorm.kafka.connect.http.model.HttpResponseItem;
import org.apache.kafka.common.Configurable;
import org.apache.kafka.connect.source.SourceRecord;

public interface SourceRecordMapper extends Configurable {

    SourceRecord map(HttpResponseItem item);
}
