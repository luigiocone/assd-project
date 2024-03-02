package it.unisannio.group8.transmission;

import org.fusesource.mqtt.client.Callback;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.concurrent.*;

public class PeriodicTransmissionStrategy implements TransmissionStrategy {
    private Callback<byte[]> callback;
    private final LocalDateTime startTime;
    private final long periodInSeconds;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final ExecutorService threadPool = Executors.newCachedThreadPool();
    private final ArrayList<byte[]> buffer;

    // Shared variable. Handles producer-consumer problem by itself
    private final BlockingQueue<byte[]> queue;

    public PeriodicTransmissionStrategy(LocalDateTime startTime, long periodInSeconds, int maxBufferSize) {
        //this.nextAlarm = startTime;
        this.startTime = startTime;
        this.periodInSeconds = (periodInSeconds > 0) ? periodInSeconds : 1;
        this.queue = new ArrayBlockingQueue<>(maxBufferSize);
        this.buffer = new ArrayList<>(maxBufferSize);
    }

    @Override
    public void init() {
        long initialDelay = ChronoUnit.SECONDS.between(LocalDateTime.now(), startTime);
        scheduler.scheduleAtFixedRate(new SendAndWait(), initialDelay, periodInSeconds, TimeUnit.SECONDS);
    }

    @Override
    public void next(byte[] payload) {
        // TODO: This method is called from inside a callback, but the documentation says:
        //  "You MUST NOT perform any blocking operations within the callback.
        //  If you need to perform some processing which MAY block, you must send
        //  it to another thread pool for processing."
        //  Study if "mqtt.blockingConnection" may be a better option
        threadPool.submit(() -> {
            // Blocking operation
            queue.add(payload);
        });
    }

    @Override
    public void setCallback(Callback<byte[]> callback) {
        this.callback = callback;
    }

    private byte[] transformBuffer() {
        // Poll all stored elements from the shared queue
        int elements = queue.drainTo(buffer);

        if (elements == 0)
            return null;

        // TODO: Create a builder to allow different transformations
        StringBuilder temp = new StringBuilder(new String(buffer.get(0)));
        for (int i = 1; i < elements; i++) {
            String str = new String(buffer.get(i));
            temp.append("\n").append(str);
        }
        return temp.toString().getBytes();
    }

    class SendAndWait implements Runnable {
        @Override
        public void run() {
            // Compute and send all the collected info
            byte[] payload = transformBuffer();
            if (payload == null) {
                System.out.println("Buffer is empty");
                return;
            }
            callback.onSuccess(payload);
        }
    }

}