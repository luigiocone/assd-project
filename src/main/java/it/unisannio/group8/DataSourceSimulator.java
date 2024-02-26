package it.unisannio.group8;

import org.fusesource.mqtt.client.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class DataSourceSimulator extends Thread {
    private final static DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
    private final String topic;
    private final String samplesPath;
    private final float rate;
    private BlockingConnection connection;

    public DataSourceSimulator(String topic, String samplesPath, float rate) throws Exception {
        this.topic = topic;
        this.samplesPath = samplesPath;
        this.rate = (rate > 0) ? rate : 1f;
    }

    @Override
    public void run() {
        try {
            // Init connection with broker
            MQTT mqtt = new MQTT();
            mqtt.setHost("tcp://localhost:1883");
            connection = mqtt.blockingConnection();
            connection.connect();

            // Reading the first sample from the samples file
            BufferedReader br = new BufferedReader(new FileReader(samplesPath));
            String line = br.readLine();
            if (line == null)
                return;
            send(line);
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
            e.printStackTrace();
        }
    }

    private void checkSleep(LocalDateTime prev, LocalDateTime curr) throws InterruptedException {
        long diff = ChronoUnit.MILLIS.between(prev, curr);
        long scaled = (long) (diff / rate);
        System.out.println("[SOURCE] Waiting " + scaled + " ms...");
        Thread.sleep(scaled);
    }

    private void send(String msg) throws Exception {
        connection.publish(topic, msg.getBytes(), QoS.AT_MOST_ONCE, false);
        System.out.println("[SOURCE] Published: " + msg);
    }
}
