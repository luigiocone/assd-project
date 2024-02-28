package it.unisannio.group8;

import it.unisannio.group8.channels.Channel;

public class EdgeNode extends Thread {
    private final Channel channel;

    public EdgeNode(Channel channel) {
        this.channel = channel;
    }

    @Override
    public void run() {
        try {
            byte[] payload = channel.receive();
            while (payload != null) {
                String msg = new String(payload);
                System.out.println("[STUB] Received: " + msg);
                payload = channel.receive();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        System.exit(0);
    }

}
