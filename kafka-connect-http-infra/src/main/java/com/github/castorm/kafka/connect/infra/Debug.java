package com.github.castorm.kafka.connect.infra;

import com.github.castorm.kafka.connect.infra.client.KafkaClient;
import com.github.castorm.kafka.connect.infra.client.KafkaConnectClient;
import io.reactivex.rxjava3.disposables.Disposable;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.util.Map;

import static com.github.castorm.kafka.connect.ConnectorUtils.readFile;
import static java.lang.Thread.sleep;

public class Debug {

    public static void main(String[] args) {

        KafkaConnectInfra infra = new KafkaConnectInfra();
        infra.start();

        Disposable subscription = createConnectorAndSubscribeTopic(infra, requestConnectorPath());
        try {
            while (!subscription.isDisposed()) sleep(1000);
        } catch (InterruptedException exception) {
            infra.stop();
        }
    }

    private static String requestConnectorPath() {
        System.out.println("Introduce the path to your connector JSON configuration file:");
        return System.console().readLine();
    }

    private static Disposable createConnectorAndSubscribeTopic(KafkaConnectInfra infra, String connectorPath) {
        Map<String, String> config = createConnector(infra.getKafkaConnectExternalRestUrl(), connectorPath);
        return subscribeToTopic(infra.getKafkaBootstrapServers(), config.get("kafka.topic"));
    }

    private static Map<String, String> createConnector(String kafkaConnectUrl, String connectorPath) {
        KafkaConnectClient connectClient = new KafkaConnectClient(kafkaConnectUrl);
        return connectClient.createConnector(readFile(connectorPath));
    }

    private static Disposable subscribeToTopic(String kafkaBootstrapServers, String topic) {
        KafkaClient kafkaClient = new KafkaClient(kafkaBootstrapServers);
        return kafkaClient.observeTopic(topic)
                .subscribe(Debug::printRecord);
    }

    private static void printRecord(ConsumerRecord<String, String> record) {
        System.out.println("Key: " + record.key() + ", Timestamp: " + record.timestamp() + "\n" + record.value());
    }
}
