package it.unisannio.group8.channels;

import org.fusesource.mqtt.client.Callback;

public interface AsyncChannel {
    void init();
    void terminate();
    void send(byte[] payload);
    void setOnRecvCallback(Callback<byte[]> callback);
}
