package com.github.castorm.kafka.connect.http.response.timestamp;

/*-
 * #%L
 * kafka-connect-http
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

import lombok.Getter;
import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.common.config.ConfigDef;

import java.time.ZoneId;
import java.util.Map;
import java.util.Optional;

import static org.apache.kafka.common.config.ConfigDef.Importance.LOW;
import static org.apache.kafka.common.config.ConfigDef.Type.STRING;

@Getter
public class NattyTimestampParserConfig extends AbstractConfig {

    private static final String ITEM_TIMESTAMP_ZONE = "http.response.record.timestamp.parser.zone";

    private final Optional<ZoneId> timestampZoneId;

    NattyTimestampParserConfig(Map<String, ?> originals) {
        super(config(), originals);
        timestampZoneId = Optional.ofNullable(getString(ITEM_TIMESTAMP_ZONE)).map(ZoneId::of);
    }

    public static ConfigDef config() {
        return new ConfigDef()
                .define(ITEM_TIMESTAMP_ZONE, STRING, null, LOW, "Timestamp ZoneId, to be used only when timestamp doesn't include a timezone/offset reference");
    }
}
