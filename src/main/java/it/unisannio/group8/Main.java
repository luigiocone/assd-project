package it.unisannio.group8;

import it.unisannio.group8.channels.*;
import it.unisannio.group8.transmission.*;
import org.fusesource.mqtt.client.MQTT;

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
        AsyncSubscriber sub = new AsyncSubscriber(topic, mqtt.callbackConnection());
        sub.setOnRecvCallback(new Callbacks.EmptyCallback<byte[]>() {
            @Override
            public void onSuccess(byte[] payload) {
                String msg = new String(payload);
                System.out.println("[STUB] Received: " + msg);
            }
        });

        sub.init();
    }

    static void startEdgeNode(String rfidTopic, String cloudTopic, MQTT mqtt) {
        AsyncPublisher pub = new AsyncPublisher(cloudTopic, mqtt.callbackConnection());
        AsyncSubscriber sub = new AsyncSubscriber(rfidTopic, mqtt.callbackConnection());
        //TransmissionStrategy strategy = new ImmediateTransmissionStrategy();
        TransmissionStrategy strategy = new PeriodicTransmissionStrategy(LocalDateTime.now(), 5, 100);

        new EdgeNode(pub, sub, strategy).start();
    }

    static void startDataSource(String topic, MQTT mqtt, String filePath) {
        AsyncChannel pub = new AsyncPublisher(topic, mqtt.callbackConnection());
        new DataSourceSimulator(pub, filePath, 1.5f).start();
    }
}
