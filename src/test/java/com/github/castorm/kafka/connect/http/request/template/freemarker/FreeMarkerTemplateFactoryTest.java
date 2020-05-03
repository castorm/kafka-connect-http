package com.github.castorm.kafka.connect.http.request.template.freemarker;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;

import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;

class FreeMarkerTemplateFactoryTest {

    FreeMarkerTemplateFactory factory = new FreeMarkerTemplateFactory();

    @Test
    void givenTemplate_whenApplyEmpty_thenAsIs() {
        assertThat(factory.create("template").apply(emptyMap())).isEqualTo("template");
    }

    @Test
    void givenTemplate_whenApplyValue_thenReplaced() {
        assertThat(factory.create("template ${key}").apply(ImmutableMap.of("key", "value"))).isEqualTo("template value");
    }
}
