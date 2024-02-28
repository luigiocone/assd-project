package it.unisannio.group8;

import it.unisannio.group8.channels.Channel;

import java.io.BufferedReader;
import java.io.FileReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class DataSourceSimulator extends Thread {
    private final static DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
    private final String samplesPath;
    private final float rate;
    private final Channel channel;

    public DataSourceSimulator(Channel channel, String samplesPath, float rate) {
        this.channel = channel;
        this.samplesPath = samplesPath;
        this.rate = (rate > 0) ? rate : 1f;
    }

    @Override
    public void run() {
        try {
            // Reading the first sample from the samples file
            BufferedReader br = new BufferedReader(new FileReader(samplesPath));
            String line = br.readLine();
            if (line == null)
                return;
            this.send(line);
            LocalDateTime curr = LocalDateTime.parse(line, FORMATTER);

            // Read each line and simulate the inter-arrivals
            line = br.readLine();
            while (line != null) {
                LocalDateTime prev = curr;
                curr = LocalDateTime.parse(line, FORMATTER);
                this.checkSleep(prev, curr);
                this.send(line);
                line = br.readLine();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void checkSleep(LocalDateTime prev, LocalDateTime curr) throws InterruptedException {
        long millis = ChronoUnit.MILLIS.between(prev, curr);
        long scaled = (long) (millis / rate);
        if (scaled <= 0)
            return;
        System.out.println("[SOURCE] Waiting " + scaled + " ms...");
        Thread.sleep(scaled);
    }

    private void send(String msg) throws Exception {
        this.channel.send(msg.getBytes());
        System.out.println("[SOURCE] Published: " + msg);
    }
}
