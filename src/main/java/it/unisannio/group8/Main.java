package it.unisannio.group8;

import it.unisannio.group8.channels.*;
import it.unisannio.group8.transmission.*;
import it.unisannio.group8.transmission.bulk.BulkBuilder;
import it.unisannio.group8.transmission.bulk.StringBulkBuilder;
import org.fusesource.mqtt.client.MQTT;
import org.fusesource.mqtt.client.QoS;

import java.io.BufferedReader;
import java.io.FileReader;
import java.time.LocalDateTime;
import java.util.Properties;

public class Main {
    final static String PROPERTIES_PATH = "src/main/resources/config.properties";

    public static void main(String[] args) throws Exception {
        // Reading properties
        BufferedReader br = new BufferedReader(new FileReader(PROPERTIES_PATH));
        Properties prop = new Properties();
        prop.load(br);

        final String brokerHost = prop.getProperty("host.broker");
        final String samplesPath = prop.getProperty("samples");
        final String fieldSeparator = prop.getProperty("fields.separator");
        final String rfidTopic = prop.getProperty("rfid.topic");
        final String cloudTopic = prop.getProperty("cloud.topic");

        // MQTT properties
        MQTT mqtt = new MQTT();
        mqtt.setHost(brokerHost);
        // mqtt.setKeepAlive((short) 60);

        // Start working threads (edge node first)
        startEdgeNode(rfidTopic, cloudTopic, mqtt);

        // Start a stub (only to check what will be sent on the cloudTopic)
        startSubscriberStub(cloudTopic, mqtt);

        Thread.sleep(1000);

        startDataSource(rfidTopic, mqtt, samplesPath);
    }

    static void startSubscriberStub(String topic, MQTT mqtt) {
        // Debug purposes
        AsyncSubscriber sub = new AsyncSubscriber(topic, QoS.AT_LEAST_ONCE, mqtt.callbackConnection());
        sub.setOnRecvCallback(new Callbacks.EmptyCallback<byte[]>() {
            @Override
            public void onSuccess(byte[] payload) {
                String msg = new String(payload);
                System.out.println("[CLOUD] Received: " + msg);
            }
        });

        sub.init();
    }

    static void startEdgeNode(String rfidTopic, String cloudTopic, MQTT mqtt) {
        AsyncPublisher pub = new AsyncPublisher(cloudTopic, QoS.AT_LEAST_ONCE, mqtt.callbackConnection());
        AsyncSubscriber sub = new AsyncSubscriber(rfidTopic, QoS.AT_LEAST_ONCE, mqtt.callbackConnection());
        BulkBuilder<String> bb = new StringBulkBuilder("\n");

        //TransmissionStrategy strategy = new ImmediateTransmissionStrategy();
        TransmissionStrategy strategy =
                new PeriodicTransmissionStrategy(LocalDateTime.now(), 5, 100, bb);

        new EdgeNode(pub, sub, strategy).start();
    }

    static void startDataSource(String topic, MQTT mqtt, String filePath) {
        AsyncChannel pub = new AsyncPublisher(topic, QoS.AT_LEAST_ONCE, mqtt.callbackConnection());
        new DataSourceSimulator(pub, filePath, 100f).start();
    }
}
