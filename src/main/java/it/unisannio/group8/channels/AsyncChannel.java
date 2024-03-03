package it.unisannio.group8.channels;

public interface AsyncChannel {
    void init() throws Exception;
    void terminate() throws Exception;
    void send(byte[] payload) throws Exception;
    void setCallback(Callback<byte[]> callback) throws Exception;
}
