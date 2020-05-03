package com.github.castorm.kafka.connect.http.request.template;

import org.junit.jupiter.api.Test;

import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;

class NoTemplateFactoryTest {

    NoTemplateFactory factory = new NoTemplateFactory();

    @Test
    void givenTemplate_whenApply_thenAsIs() {
        assertThat(factory.create("template").apply(emptyMap())).isEqualTo("template");
    }
}
