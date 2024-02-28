package it.unisannio.group8.channels;

import java.io.Serializable;

public interface Channel {
    void send(byte[] payload);
    byte[] receive();
}
