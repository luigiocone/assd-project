package it.unisannio.group8.transmission;


import it.unisannio.group8.channels.Callback;

public class ImmediateTransmissionStrategy implements TransmissionStrategy {
    private Callback<byte[]> callback;

    @Override
    public void init() { }

    @Override
    public void next(byte[] payload) {
        if (callback != null)
            callback.onEvent(payload);
    }

    @Override
    public void setCallback(Callback<byte[]> callback) {
        this.callback = callback;
    }
}
