package it.unisannio.group8;

import it.unisannio.group8.channels.*;
import org.fusesource.mqtt.client.MQTT;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Properties;

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
        AsyncSubscriber sub = new AsyncSubscriber(topic, mqtt.callbackConnection());
        new EdgeNode(sub).start();
        Thread.sleep(1000);

        AsyncChannel pub = new AsyncPublisher(topic, mqtt.callbackConnection());
        new DataSourceSimulator(pub, samplesPath, 2.5f).start();
    }
}
