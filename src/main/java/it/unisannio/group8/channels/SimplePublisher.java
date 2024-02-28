package it.unisannio.group8.channels;

import org.fusesource.mqtt.client.BlockingConnection;
import org.fusesource.mqtt.client.QoS;

public class SimplePublisher implements Channel {
    private final String topic;
    private final BlockingConnection connection;

    public SimplePublisher(String topic, BlockingConnection connection) throws Exception {
        this.topic = topic;
        this.connection = connection;

        // Init connection with broker
        if (!connection.isConnected())
            connection.connect();
    }

    public void publish(byte[] payload) throws Exception {
        connection.publish(topic, payload, QoS.AT_MOST_ONCE, false);
    }

    @Override
    public void send(byte[] message) {
        try {
            this.publish(message);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte[] receive() { return null; }
}
