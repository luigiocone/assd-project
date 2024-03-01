package it.unisannio.group8;

import it.unisannio.group8.channels.AsyncChannel;
import it.unisannio.group8.channels.Callbacks;
import org.fusesource.mqtt.client.Callback;

public class EdgeNode {
    private final AsyncChannel channel;
    private final Callback<byte[]> printCallback = new Callbacks.EmptyCallback<byte[]>() {
        @Override
        public void onSuccess(byte[] bytes) {
            String msg = new String(bytes);
            System.out.println("[STUB] Received: " + msg);
        }
    };

    public EdgeNode(AsyncChannel channel) {
        this.channel = channel;
        this.channel.setOnRecvCallback(printCallback);
    }

    public void start() {
        this.channel.init();
    }
}
