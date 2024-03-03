package it.unisannio.group8;

import it.unisannio.group8.channels.AsyncChannel;
import it.unisannio.group8.channels.Callbacks;
import it.unisannio.group8.transmission.TransmissionStrategy;

public class EdgeNode {
    private final AsyncChannel sender;
    private final AsyncChannel receiver;
    private final TransmissionStrategy strategy;

    public EdgeNode(AsyncChannel sender, AsyncChannel receiver, TransmissionStrategy strategy) {
        this.sender = sender;
        this.receiver = receiver;
        this.strategy = strategy;
    }

    public void start() {
        // Edge node passes all the received messages to the strategy object
        receiver.setOnRecvCallback(new Callbacks.EmptyCallback<byte[]>() {
            @Override
            public void onSuccess(byte[] payload) {
                strategy.next(payload);
            }
        });

        // Transmission strategy will send a byte array when needed
        strategy.setCallback(new Callbacks.EmptyCallback<byte[]>() {
            @Override
            public void onSuccess(byte[] payload) {
                sender.send(payload);
            }
        });

        receiver.init();
        strategy.init();
        sender.init();
    }
}
