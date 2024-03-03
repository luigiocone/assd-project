package it.unisannio.group8.transmission;

import it.unisannio.group8.channels.Callback;

public interface TransmissionStrategy {
    public void init();
    public void next(byte[] payload);
    public void setCallback(Callback<byte[]> callback);
}
