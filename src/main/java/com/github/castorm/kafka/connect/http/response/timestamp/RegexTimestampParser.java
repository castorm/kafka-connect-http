package com.github.castorm.kafka.connect.http.response.timestamp;

/*-
 * #%L
 * Kafka Connect HTTP Plugin
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

import com.github.castorm.kafka.connect.http.response.timestamp.spi.TimestampParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RequiredArgsConstructor
@Slf4j
public class RegexTimestampParser implements TimestampParser {

  private final Function<Map<String, ?>, RegexTimestampParserConfig> configFactory;
  private Pattern pattern;
  private TimestampParser delegate;
  
  public RegexTimestampParser() {
    this(RegexTimestampParserConfig::new);
  }

  @Override
  public Instant parse(String timestamp) {
    Matcher matcher = pattern.matcher(timestamp);
    String extractedTimestamp;
    matcher.find();
    extractedTimestamp = matcher.group(1);
    return delegate.parse(extractedTimestamp);
  }

  @Override
  public void configure(Map<String, ?> settings) {
    RegexTimestampParserConfig config = configFactory.apply(settings);
    pattern = Pattern.compile(config.getTimestampRegex());
    delegate = config.getDelegateParser();
  }
}
