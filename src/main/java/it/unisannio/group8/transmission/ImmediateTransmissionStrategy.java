package it.unisannio.group8.transmission;

import org.fusesource.mqtt.client.Callback;

public class ImmediateTransmissionStrategy implements TransmissionStrategy {
    private Callback<byte[]> callback;

    @Override
    public void init() { }

    @Override
    public void next(byte[] payload) {
        if (callback != null)
            callback.onSuccess(payload);
    }

    @Override
    public void setCallback(Callback<byte[]> callback) {
        this.callback = callback;
    }
}
