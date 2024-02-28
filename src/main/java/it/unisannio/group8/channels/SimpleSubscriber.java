package it.unisannio.group8.channels;

import org.fusesource.mqtt.client.BlockingConnection;
import org.fusesource.mqtt.client.Message;
import org.fusesource.mqtt.client.QoS;
import org.fusesource.mqtt.client.Topic;

import java.util.concurrent.TimeUnit;

public class SimpleSubscriber implements Channel {
    final String topic;
    final BlockingConnection connection;

    public SimpleSubscriber(String topic, BlockingConnection connection) throws Exception {
        this.topic = topic;
        this.connection = connection;

        // Init connection with broker
        if (!connection.isConnected())
            connection.connect();

        // Subscribe to topic
        Topic[] topics = { new Topic(topic, QoS.AT_MOST_ONCE) };
        connection.subscribe(topics);
    }

    @Override
    public void send(byte[] message) { }

    @Override
    public byte[] receive() {
        try {
            Message msg = connection.receive(15, TimeUnit.SECONDS);
            return msg.getPayload();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
