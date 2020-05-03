package com.github.castorm.kafka.connect.http.request.template;

import com.github.castorm.kafka.connect.http.request.template.spi.TemplateFactory;
import com.github.castorm.kafka.connect.http.request.template.spi.Template;

public class NoTemplateFactory implements TemplateFactory {

    @Override
    public Template create(String template) {
        return offset -> template;
    }
}
