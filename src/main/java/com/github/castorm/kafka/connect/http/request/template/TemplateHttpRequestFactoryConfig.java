package com.github.castorm.kafka.connect.http.request.template;

import com.github.castorm.kafka.connect.http.request.template.spi.TemplateFactory;
import lombok.Getter;
import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.common.config.ConfigDef;

import java.util.Map;

import static org.apache.kafka.common.config.ConfigDef.Importance.HIGH;
import static org.apache.kafka.common.config.ConfigDef.Importance.LOW;
import static org.apache.kafka.common.config.ConfigDef.Importance.MEDIUM;
import static org.apache.kafka.common.config.ConfigDef.Type.CLASS;
import static org.apache.kafka.common.config.ConfigDef.Type.STRING;

@Getter
public class TemplateHttpRequestFactoryConfig extends AbstractConfig {

    private static final String URL = "http.source.url";
    private static final String METHOD = "http.source.method";
    private static final String HEADERS = "http.source.headers";
    private static final String QUERY_PARAMS = "http.source.query-params";
    private static final String BODY = "http.source.body";
    private static final String TEMPLATE_FACTORY = "http.source.template.factory";

    private final String url;

    private final String method;

    private final String headers;

    private final String queryParams;

    private final String body;

    private final TemplateFactory templateFactory;

    TemplateHttpRequestFactoryConfig(Map<String, ?> originals) {
        super(config(), originals);
        url = getString(URL);
        method = getString(METHOD);
        headers = getString(HEADERS);
        queryParams = getString(QUERY_PARAMS);
        body = getString(BODY);
        templateFactory = getConfiguredInstance(TEMPLATE_FACTORY, TemplateFactory.class);
    }

    public static ConfigDef config() {
        return new ConfigDef()
                .define(URL, STRING, HIGH, "HTTP URL Template")
                .define(METHOD, STRING, "GET", HIGH, "HTTP Method Template")
                .define(HEADERS, STRING, "", MEDIUM, "HTTP Headers Template")
                .define(QUERY_PARAMS, STRING, "", MEDIUM, "HTTP Query Params Template")
                .define(BODY, STRING, "", LOW, "HTTP Body Template")
                .define(TEMPLATE_FACTORY, CLASS, NoTemplateFactory.class, LOW, "Template Factory Class");
    }
}
