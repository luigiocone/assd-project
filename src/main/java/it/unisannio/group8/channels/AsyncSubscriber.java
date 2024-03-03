package it.unisannio.group8.channels;

import org.eclipse.paho.client.mqttv3.*;

public class AsyncSubscriber implements AsyncChannel {
    private final String topic;
    private final int qos;
    private final IMqttAsyncClient client;

    public AsyncSubscriber(String topic, int qos, IMqttAsyncClient client) {
        this.topic = topic;
        this.qos = qos;
        this.client = client;
    }

    public AsyncSubscriber(String topic, int qos, IMqttAsyncClient client, MqttCallback callback) {
        this(topic, qos, client);
        this.client.setCallback(callback);
    }

    @Override
    public void init() throws MqttException {
        if (client.isConnected()) {
            client.subscribe(topic, qos);
            return;
        }

        IMqttToken token = client.connect();
        token.waitForCompletion();
        client.subscribe(topic, qos);
    }

    @Override
    public void send(byte[] message) { }

    @Override
    public void setCallback(Callback<byte[]> callback) throws Exception {
        client.setCallback(new MqttCallback() {
            @Override public void connectionLost(Throwable cause) { }
            @Override public void deliveryComplete(IMqttDeliveryToken token) { }
            @Override
            public void messageArrived(String topic, MqttMessage message) {
                callback.onEvent(message.getPayload());
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
