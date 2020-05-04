package com.github.castorm.kafka.connect.http.request.template.freemarker;

/*-
 * #%L
 * kafka-connect-http-plugin
 * %%
 * Copyright (C) 2020 CastorM
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.github.castorm.kafka.connect.http.request.template.spi.Template;
import com.github.castorm.kafka.connect.http.request.template.spi.TemplateFactory;
import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import freemarker.template.Version;
import lombok.SneakyThrows;

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
        return offset -> apply(createTemplate(template), offset);
    }

    @SneakyThrows(IOException.class)
    private freemarker.template.Template createTemplate(String template) {
        return new freemarker.template.Template(randomUUID().toString(), new StringReader(template), configuration);
    }

    @SneakyThrows({TemplateException.class, IOException.class})
    private String apply(freemarker.template.Template template, Map<String, ?> model) {
        Writer writer = new StringWriter();
        template.process(model, writer);
        return writer.toString();
    }
}
