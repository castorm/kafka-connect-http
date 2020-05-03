package com.github.castorm.kafka.connect.http.request.template.spi;

@FunctionalInterface
public interface TemplateFactory {

    Template create(String template);
}
