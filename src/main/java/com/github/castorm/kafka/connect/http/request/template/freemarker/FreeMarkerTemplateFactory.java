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

import com.github.castorm.kafka.connect.http.request.template.spi.Template;
import com.github.castorm.kafka.connect.http.request.template.spi.TemplateFactory;
import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import freemarker.template.Version;
import lombok.SneakyThrows;
import lombok.Value;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;

import static java.util.UUID.randomUUID;

public class FreeMarkerTemplateFactory implements TemplateFactory {

    private final Configuration configuration = new Configuration(new Version(2, 3, 30));

    @Override
    public Template create(String template) {
        return offset -> apply(createTemplate(template), new TemplateModel(offset.toMap()));
    }

    @SneakyThrows(IOException.class)
    private freemarker.template.Template createTemplate(String template) {
        return new freemarker.template.Template(randomUUID().toString(), new StringReader(template), configuration);
    }

    @SneakyThrows({TemplateException.class, IOException.class})
    private String apply(freemarker.template.Template template, TemplateModel model) {
        Writer writer = new StringWriter();
        template.process(model, writer);
        return writer.toString();
    }

    @Value
    public static class TemplateModel {

        Map<String, ?> offset;
    }
}
