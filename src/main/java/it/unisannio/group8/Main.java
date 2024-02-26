package it.unisannio.group8;

import org.fusesource.mqtt.client.*;

import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) throws Exception {
        String path = "src/main/resources/samples.txt";
        String topic = "rfid/0";
        new StubSubscriber(topic).start();
        Thread.sleep(1000);
        new DataSourceSimulator(topic, path, 2.5f).start();
    }
}

// Temporary class. It only shows messages on passed topic
class StubSubscriber extends Thread {
    String topic;

    public StubSubscriber(String topic) {
        this.topic = topic;
    }

    @Override
    public void run() {
        try {
            // Connect to broker
            MQTT mqtt = new MQTT();
            mqtt.setHost("tcp://localhost:1883");
            BlockingConnection connection = mqtt.blockingConnection();
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
            e.printStackTrace();
            System.exit(1);
        }
        System.exit(0);
    }
}
