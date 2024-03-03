package it.unisannio.group8.channels;

import org.fusesource.mqtt.client.Callback;
import org.fusesource.mqtt.client.CallbackConnection;
import org.fusesource.mqtt.client.QoS;

public class AsyncPublisher implements AsyncChannel {
    private final String topic;
    private final QoS qos;
    private final CallbackConnection connection;
    private Callback<Void> onSend = new Callbacks.EmptyCallback<>();

    public AsyncPublisher(String topic, QoS qos, CallbackConnection connection) {
        this.topic = topic;
        this.connection = connection;
        this.qos = qos;
    }

    public AsyncPublisher(String topic, QoS qos, CallbackConnection connection, Callback<Void> onSend) {
        this(topic, qos, connection);
        this.onSend = onSend;
    }

    @Override
    public void init() {
        connection.connect(Callbacks.SIGNAL_FAILURE);
    }

    @Override
    public void terminate() {
        connection.disconnect(Callbacks.SIGNAL_FAILURE);
    }

    @Override
    public void send(byte[] message) {
        connection.publish(topic, message, qos, false, onSend);
    }

    @Override
    public void setOnRecvCallback(Callback<byte[]> callback) { }
}
