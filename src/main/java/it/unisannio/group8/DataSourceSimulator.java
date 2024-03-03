package it.unisannio.group8;

import it.unisannio.group8.channels.AsyncChannel;

import java.io.BufferedReader;
import java.io.FileReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class DataSourceSimulator extends Thread {
    private final static DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final String samplesPath;
    private final float rate;
    private final AsyncChannel sender;

    public DataSourceSimulator(AsyncChannel sender, String samplesPath, float rate) {
        this.sender = sender;
        this.samplesPath = samplesPath;
        this.rate = (rate > 0) ? rate : 1f;
    }

    @Override
    public void run() {
        try {
            sender.init();

            // Read the first sample from the samples file
            BufferedReader br = new BufferedReader(new FileReader(samplesPath));
            String line = br.readLine();
            if (line == null) {
                end();
                return;
            }
            send(line);

            // Read each line and simulate the inter-arrivals from the read timestamps
            LocalDateTime curr = getDateTime(line);
            line = br.readLine();
            while (line != null) {
                LocalDateTime prev = curr;
                curr = getDateTime(line);
                checkSleep(prev, curr);
                send(line);
                line = br.readLine();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            end();
        }
    }

    private LocalDateTime getDateTime(String line) {
        String dt = line.split(",", 3)[1];
        return LocalDateTime.parse(dt, FORMATTER);
    }

    private void checkSleep(LocalDateTime prev, LocalDateTime curr) throws InterruptedException {
        long millis = ChronoUnit.MILLIS.between(prev, curr);
        long scaled = (long) (millis / rate);
        if (scaled > 0) {
            Thread.sleep(scaled);
        }
    }

    private void send(String msg) {
        sender.send(msg.getBytes());
        //System.out.println("[SOURCE] Published: " + msg);
    }

    private void end() {
        sender.terminate();
        System.out.println("[SOURCE] Terminated");
    }
}
