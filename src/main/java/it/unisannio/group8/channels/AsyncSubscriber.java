package it.unisannio.group8.channels;

import org.fusesource.hawtbuf.Buffer;
import org.fusesource.hawtbuf.UTF8Buffer;
import org.fusesource.mqtt.client.*;

public class AsyncSubscriber implements AsyncChannel {
    private final String topic;
    private final CallbackConnection connection;
    private Callback<byte[]> onReceive = new Callbacks.EmptyCallback<>();

    public AsyncSubscriber(String topic, CallbackConnection connection) {
        this.topic = topic;
        this.connection = connection;
    }

    public AsyncSubscriber(String topic, CallbackConnection connection, Callback<byte[]> onReceive) {
        this(topic, connection);
        this.onReceive = onReceive;
    }

    @Override
    public void init() {
        // Set a connection listener to handle received messages and connection events
        connection.listener(new ExtendedListener() {
            @Override public void onConnected() { }
            @Override public void onPublish(UTF8Buffer utf8Buffer, Buffer buffer, Runnable ack) { }

            @Override public void onFailure(Throwable throwable) {
                onReceive.onFailure(throwable);
            }

            @Override
            public void onDisconnected() {
                System.err.println("Subscriber disconnected");
            }

            @Override
            public void onPublish(UTF8Buffer utf8Buffer, Buffer buffer, Callback<Callback<Void>> callback) {
                byte[] payload = buffer.toByteArray();
                onReceive.onSuccess(payload);
            }
        });

        // Connect to broker. Subscribe to passed topic if connection was successful
        connection.connect(new Callbacks.EmptyCallback<Void>() {
            @Override
            public void onSuccess(Void unused) {
                subscribe();
            }
        });
    }

    @Override
    public void send(byte[] message) { }

    @Override
    public void setOnRecvCallback(Callback<byte[]> callback) {
        this.onReceive = callback;
    }

    @Override
    public void terminate() {
        connection.disconnect(Callbacks.SIGNAL_FAILURE);
    }

    private void subscribe() {
        // Following callback doesn't make anything after a successful subscription
        Callback<byte[]> cb = new Callbacks.EmptyCallback<>();
        Topic[] topics = { new Topic(topic, QoS.AT_MOST_ONCE) };
        connection.subscribe(topics, cb);
    }
}
