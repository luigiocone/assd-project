package it.unisannio.group8;

import org.fusesource.mqtt.client.*;

import java.io.*;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class Main {
    final static String PROPERTIES_PATH = "src/main/resources/config.properties";

    public static void main(String[] args) throws Exception {
        // Reading properties
        BufferedReader br = new BufferedReader(new FileReader(PROPERTIES_PATH));
        Properties prop = new Properties();
        prop.load(br);

        final String brokerHost = prop.getProperty("host.broker");
        final String samplesPath = prop.getProperty("samples");
        final String topic = prop.getProperty("topic");

        MQTT mqtt = new MQTT();
        mqtt.setHost(brokerHost);

        // Starting subscriber first
        new StubSubscriber(mqtt.blockingConnection(), topic).start();
        Thread.sleep(1000);
        new DataSourceSimulator(mqtt.blockingConnection(), topic, samplesPath, 2.5f).start();
    }
}

// Temporary class. It only shows messages on passed topic
class StubSubscriber extends Thread {
    final String topic;
    final BlockingConnection connection;

    public StubSubscriber(BlockingConnection connection, String topic) {
        this.connection = connection;
        this.topic = topic;
    }

    @Override
    public void run() {
        try {
            // Connect to broker
            if (!connection.isConnected())
                connection.connect();

            // Subscribe to topic
            Topic[] topics = { new Topic(topic, QoS.AT_MOST_ONCE) };
            connection.subscribe(topics);

            // Timeout after 15 seconds
            Message msg = connection.receive(15, TimeUnit.SECONDS);
            while (msg != null) {
                String payload = new String(msg.getPayload());
                System.out.println("[STUB] Received: " + payload);
                msg = connection.receive(15, TimeUnit.SECONDS);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        System.exit(0);
    }
}
