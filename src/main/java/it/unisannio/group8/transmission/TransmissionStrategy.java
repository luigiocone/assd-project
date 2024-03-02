package it.unisannio.group8.transmission;

import org.fusesource.mqtt.client.Callback;

public interface TransmissionStrategy {
    public void init();
    public void next(byte[] payload);
    public void setCallback(Callback<byte[]> callback);
}
