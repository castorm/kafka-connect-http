package com.github.castorm.kafka.connect.http;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opensearch.testcontainers.OpensearchContainer;
import org.testcontainers.utility.DockerImageName;

class StreamkapElasticConnectorTest {

    private static OpensearchContainer<?> opensearch = new OpensearchContainer<>(
        DockerImageName.parse("opensearchproject/opensearch:2.11.0"));
        HttpSourceConnector connector = new HttpSourceConnector();

    @BeforeAll
    static void before() {
        opensearch.start();
    }

    @AfterAll
    static void after() {
        opensearch.stop();
    }

    @Test
    void testNominal() throws Exception {
        sendRequest("/index1", "POST", "{ \"my_timestamp\": \"" + Instant.now().toString() + "\", \"message\": \"Hello OpenSearch 1\" }");
        sendRequest("/index2", "POST", "{ \"my_timestamp\": \"" + Instant.now().toString() + "\", \"message\": \"Hello OpenSearch 2\" }");
    }

    private void sendRequest(String urlPath, String method, String jsonInputString) throws Exception {
        URL url = new URL(opensearch.getHttpHostAddress() + urlPath);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod(method);
        con.setRequestProperty("Content-Type", "application/json; utf-8");
        con.setRequestProperty("Accept", "application/json");
        con.setDoOutput(true);

        try(OutputStream os = con.getOutputStream()) {
            byte[] input = jsonInputString.getBytes("utf-8");
            os.write(input, 0, input.length);			
        }

        int responseCode = con.getResponseCode();
        System.out.println(responseCode); // Handle response code appropriately
    }
}
