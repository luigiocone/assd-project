package it.unisannio.group8;

import it.unisannio.group8.channels.*;
import it.unisannio.group8.model.DisposalSampleFactory;
import it.unisannio.group8.transmission.*;
import it.unisannio.group8.transmission.bulk.BulkBuilder;
import it.unisannio.group8.transmission.bulk.JsonBulkBuilder;
import it.unisannio.group8.transmission.bulk.StringBulkBuilder;
import org.eclipse.paho.client.mqttv3.IMqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

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
    final static int DEFAULT_EDGE_BUFFER_SIZE = 250;
    final static int DEFAULT_QOS = 1; // AT_LEAST_ONCE

    static String brokerHost;
    static String fieldSeparator;


    public static void main(String[] args) throws Exception {
        // Reading properties
        BufferedReader br = new BufferedReader(new FileReader(PROPERTIES_PATH));
        Properties prop = new Properties();
        prop.load(br);

        brokerHost = prop.getProperty("host.broker");
        fieldSeparator = prop.getProperty("fields.separator");
        final String samplesDir = prop.getProperty("samples.dir");
        final String rfidTopic = prop.getProperty("rfid.topic");
        final String cloudTopic = prop.getProperty("cloud.topic");

        // Checking the samples dir and its files
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
        for (int i = 0; i < fileNames.size(); i++) {
            startEdgeNode("EDGE" + i, rfidTopics.get(i), cloudTopic);
        }
        System.out.println("done.");

        // Start a stub (only to check what will be sent on the cloudTopic)
        System.out.print("Starting a subscriber stub... ");
        startSubscriberStub("STUB", cloudTopic);
        System.out.println("done.");

        Thread.sleep(1000);

        // Start data sources
        System.out.print("Starting data sources... ");
        for (int i = 0; i < fileNames.size(); i++) {
            startDataSource("SOURCE" + i, rfidTopics.get(i), fileNames.get(i));
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
                //break;   // Only one data source
            }
        }
        return names;
    }

    static void startSubscriberStub(String clientId, String rfidTopic) throws Exception {
        // Debug purposes
        IMqttAsyncClient client = new MqttAsyncClient(brokerHost, clientId, new MemoryPersistence());
        AsyncSubscriber sub = new AsyncSubscriber(rfidTopic, DEFAULT_QOS, client);
        sub.setCallback(payload -> {
            String msg = new String(payload);
            System.out.println("[CLOUD] Received: " + msg);
        });

        sub.init();
    }

    static void startEdgeNode(String clientId, String rfidTopic, String cloudTopic) throws Exception {
        IMqttAsyncClient client = new MqttAsyncClient(brokerHost, clientId, new MemoryPersistence());
        AsyncPublisher pub = new AsyncPublisher(cloudTopic, DEFAULT_QOS, client);
        AsyncSubscriber sub = new AsyncSubscriber(rfidTopic, DEFAULT_QOS, client);

        // BulkBuilder to aggregate multiple payloads in one bulk
        BulkBuilder<String> bb =
                new StringBulkBuilder("\n");
                //new JsonBulkBuilder(new DisposalSampleFactory(fieldSeparator));

        // Strategy on how to handle a sample arriving at the edge node
        TransmissionStrategy strategy =
                //new ImmediateTransmissionStrategy();
                new PeriodicTransmissionStrategy(LocalDateTime.now(), 5, DEFAULT_EDGE_BUFFER_SIZE, bb);

        new EdgeNode(pub, sub, strategy).start();
    }

    static void startDataSource(String clientId, String topic, String filePath) throws MqttException {
        IMqttAsyncClient client = new MqttAsyncClient(brokerHost, clientId, new MemoryPersistence());
        AsyncChannel pub = new AsyncPublisher(topic, DEFAULT_QOS, client);
        new DataSourceSimulator(pub, filePath, 100f).start();
    }
}
