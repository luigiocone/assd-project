package it.unisannio.group8;

import it.unisannio.group8.channels.*;
import it.unisannio.group8.transmission.*;
import it.unisannio.group8.transmission.bulk.BulkBuilder;
import it.unisannio.group8.transmission.bulk.StringBulkBuilder;
import org.fusesource.mqtt.client.MQTT;
import org.fusesource.mqtt.client.QoS;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class Main {
    final static String PROPERTIES_PATH = "src/main/resources/config.properties";
    final static QoS DEFAULT_QOS = QoS.AT_MOST_ONCE;

    public static void main(String[] args) throws Exception {
        // Reading properties
        BufferedReader br = new BufferedReader(new FileReader(PROPERTIES_PATH));
        Properties prop = new Properties();
        prop.load(br);

        final String brokerHost = prop.getProperty("host.broker");
        final String samplesDir = prop.getProperty("samples.dir");
        final String fieldSeparator = prop.getProperty("fields.separator");
        final String rfidTopic = prop.getProperty("rfid.topic");
        final String cloudTopic = prop.getProperty("cloud.topic");

        // MQTT properties
        MQTT mqtt = new MQTT();
        mqtt.setHost(brokerHost);

        List<String> fileNames = getSamplesFiles(samplesDir);
        System.out.println(fileNames.size() + " files found in " + samplesDir);

        // Build topics name
        List<String> rfidTopics = new ArrayList<>(fileNames.size());
        for (int i = 0; i < fileNames.size(); i++) {
            String name = fileNames.get(i);
            rfidTopics.add(rfidTopic + name);

            Path p = Paths.get(samplesDir, name);
            fileNames.set(i, p.toString());
        }

        // Start edge nodes
        System.out.print("Starting edge nodes... ");
        for (String t: rfidTopics) {
            startEdgeNode(t, cloudTopic, mqtt);
        }
        System.out.println("done.");

        // Start a stub (only to check what will be sent on the cloudTopic)
        System.out.print("Starting a subscriber stub... ");
        startSubscriberStub(cloudTopic, mqtt);
        System.out.println("done.");

        Thread.sleep(1000);

        // Start data sources
        System.out.print("Starting data sources... ");
        for (int i = 0; i < fileNames.size(); i++) {
            startDataSource(rfidTopics.get(i), mqtt, fileNames.get(i));
        }
        System.out.println("done.");
    }

    static List<String> getSamplesFiles(String samplesDir) {
        // Read file names in passed dir
        File folder = new File(samplesDir);
        File[] listOfFiles = folder.listFiles();
        assert listOfFiles != null;

        ArrayList<String> names = new ArrayList<>();
        for (File f: listOfFiles) {
            if (f.isFile()) {
                names.add(f.getName());
                // TODO: Delete break. Using only one file
                break;
            }
        }
        return names;
    }

    static void startSubscriberStub(String topic, MQTT mqtt) {
        // Debug purposes
        AsyncSubscriber sub = new AsyncSubscriber(topic, DEFAULT_QOS, mqtt.callbackConnection());
        sub.setOnRecvCallback(new Callbacks.EmptyCallback<byte[]>() {
            @Override
            public void onSuccess(byte[] payload) {
                String msg = new String(payload);
                System.out.println("[CLOUD] Received: " + msg);
            }
        });

        sub.init();
    }

    static void startEdgeNode(String rfidTopic, String cloudTopic, MQTT mqtt) {
        AsyncPublisher pub = new AsyncPublisher(cloudTopic, DEFAULT_QOS, mqtt.callbackConnection());
        AsyncSubscriber sub = new AsyncSubscriber(rfidTopic, DEFAULT_QOS, mqtt.callbackConnection());
        BulkBuilder<String> bb = new StringBulkBuilder("\n");

        //TransmissionStrategy strategy = new ImmediateTransmissionStrategy();
        TransmissionStrategy strategy =
                new PeriodicTransmissionStrategy(LocalDateTime.now(), 5, 100, bb);

        new EdgeNode(pub, sub, strategy).start();
    }

    static void startDataSource(String topic, MQTT mqtt, String filePath) {
        AsyncChannel pub = new AsyncPublisher(topic, DEFAULT_QOS, mqtt.callbackConnection());
        new DataSourceSimulator(pub, filePath, 100f).start();
    }
}
