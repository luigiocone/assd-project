package it.unisannio.group8.channels;

import org.eclipse.paho.client.mqttv3.*;

public class AsyncPublisher implements AsyncChannel {
    private final String topic;
    private final int qos;
    private final IMqttAsyncClient client;

    public AsyncPublisher(String topic, int qos, IMqttAsyncClient client) {
        this.topic = topic;
        this.qos = qos;
        this.client = client;
    }

    public AsyncPublisher(String topic, int qos, IMqttAsyncClient client, MqttCallback callback) {
        this(topic, qos, client);
        this.client.setCallback(callback);
    }

    @Override
    public void init() throws MqttException {
        if (!client.isConnected()) {
            IMqttToken token = client.connect();
            token.waitForCompletion();
        }
    }

    @Override
    public void send(byte[] payload) throws MqttException{
        client.publish(topic, payload, qos, false);
    }

    @Override
    public void setCallback(Callback<byte[]> callback) throws Exception {
        client.setCallback(new MqttCallback() {
            @Override public void connectionLost(Throwable cause) { }
            @Override public void messageArrived(String topic, MqttMessage message) { }
            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                callback.onEvent(null);
            }
        });
    }

    public void setCallback(MqttCallback callback) {
        client.setCallback(callback);
    }

    @Override
    public void terminate() throws Exception {
        client.disconnect();
    }
}
