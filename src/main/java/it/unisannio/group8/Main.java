package it.unisannio.group8;

import it.unisannio.group8.channels.Channel;
import it.unisannio.group8.channels.SimplePublisher;
import it.unisannio.group8.channels.SimpleSubscriber;
import org.fusesource.mqtt.client.*;

import java.io.*;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class Main {
    final static String PROPERTIES_PATH = "src/main/resources/config.properties";

    public static void main(String[] args) throws Exception {
        // Reading properties
        BufferedReader br = new BufferedReader(new FileReader(PROPERTIES_PATH));
        Properties prop = new Properties();
        prop.load(br);

        final String brokerHost = prop.getProperty("host.broker");
        final String samplesPath = prop.getProperty("samples");
        final String topic = prop.getProperty("topic");

        MQTT mqtt = new MQTT();
        mqtt.setHost(brokerHost);

        // Starting subscriber first
        Channel sub = new SimpleSubscriber(topic, mqtt.blockingConnection());
        new EdgeNode(sub).start();

        Thread.sleep(1000);

        Channel pub = new SimplePublisher(topic, mqtt.blockingConnection());
        new DataSourceSimulator(pub, samplesPath, 2.5f).start();
    }
}
