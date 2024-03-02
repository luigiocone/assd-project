package it.unisannio.group8.transmission;

import org.fusesource.mqtt.client.Callback;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PeriodicTransmissionStrategy implements TransmissionStrategy {
    private Callback<byte[]> callback;
    private final LocalDateTime startTime;
    private final long periodInSeconds;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final ExecutorService threadPool = Executors.newCachedThreadPool();

    // Shared variable
    // TODO: A "producer-consumer circular array" could be more efficient
    private final ArrayList<byte[]> buffer = new ArrayList<>();

    public PeriodicTransmissionStrategy(LocalDateTime startTime, long periodInSeconds) {
        this.startTime = startTime;
        //this.nextAlarm = startTime;
        this.periodInSeconds = (periodInSeconds > 0) ? periodInSeconds : 1;
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
            synchronized (buffer) {
                buffer.add(payload);
            }
        });
    }

    @Override
    public void setCallback(Callback<byte[]> callback) {
        this.callback = callback;
    }

    private byte[] transformBuffer() {
        // Copy the shared arraylist
        ArrayList<byte[]> copy;
        synchronized (buffer) {
            copy = new ArrayList<>(buffer);
            buffer.clear();
        }

        if (copy.isEmpty())
            return null;

        // TODO: Create a builder to allow different transformations
        StringBuilder temp = new StringBuilder(new String(copy.get(0)));
        for (int i = 1; i < copy.size(); i++) {
            String str = new String(copy.get(i));
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