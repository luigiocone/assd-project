package it.unisannio.group8;

import it.unisannio.group8.channels.AsyncChannel;
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

    public void start() throws Exception {
        // Edge node passes all the received messages to the strategy object
        receiver.setCallback(payload -> strategy.next(payload));

        // Transmission strategy will send a byte array when needed
        strategy.setCallback(payload -> {
            try {
                sender.send(payload);
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        receiver.init();
        strategy.init();
        sender.init();
    }
}
