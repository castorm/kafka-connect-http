package com.github.castorm.kafka.connect.http.record;
import com.github.castorm.kafka.connect.http.model.Offset;
import com.github.castorm.kafka.connect.http.record.spi.SourceRecordFilterFactory;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.connect.source.SourceRecord;

import java.time.Instant;
import java.util.function.Predicate;

@RequiredArgsConstructor
public class OffsetIdentityRecordFilterFactory implements SourceRecordFilterFactory {


    @Override
    public Predicate<SourceRecord> create(Offset offset) {
        String offsetKey = offset.getKey().get();
        return record -> record.sourceOffset().toString().equals(offsetKey);
    }
    

}