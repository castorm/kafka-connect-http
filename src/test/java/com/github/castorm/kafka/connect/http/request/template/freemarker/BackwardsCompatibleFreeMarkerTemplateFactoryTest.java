package com.github.castorm.kafka.connect.http.request.template.freemarker;

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

import com.github.castorm.kafka.connect.http.model.Offset;
import com.github.castorm.kafka.connect.http.model.Partition;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;

import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;

class BackwardsCompatibleFreeMarkerTemplateFactoryTest {

    BackwardsCompatibleFreeMarkerTemplateFactory factory = new BackwardsCompatibleFreeMarkerTemplateFactory();

    @Test
    void givenTemplate_whenApplyEmpty_thenAsIs() {
        assertThat(factory.create("template").apply(Partition.of(emptyMap()), Offset.of(emptyMap()))).isEqualTo("template");
    }

    @Test
    void givenTemplate_whenApplyValue_thenReplaced() {
        Partition partition = Partition.of(ImmutableMap.of("key", "partition1"));
        Offset offset = Offset.of(ImmutableMap.of("key", "offset1"));
        assertThat(factory.create("template ${partition.key} ${offset.key}").apply(partition, offset)).isEqualTo("template partition1 offset1");
    }

    @Test
    void givenTemplate_whenApplyOffsetValue_thenReplacedWithoutNamespace() {
        Offset offset = Offset.of(ImmutableMap.of("key", "offset1"));
        assertThat(factory.create("template ${key}").apply(Partition.of(emptyMap()), offset)).isEqualTo("template offset1");
    }
}
