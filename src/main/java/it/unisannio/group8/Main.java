package it.unisannio.group8;

import it.unisannio.group8.channels.*;
import it.unisannio.group8.model.DisposalSampleFactory;
import it.unisannio.group8.transmission.*;
import it.unisannio.group8.transmission.bulk.BulkBuilder;
import it.unisannio.group8.transmission.bulk.JsonBulkBuilder;
import it.unisannio.group8.transmission.bulk.StringBulkBuilder;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Main {
    final static String PROPERTIES_PATH = "src/main/resources/config.properties";
    static AppProperties prop;

    public static void main(String[] args) throws Exception {
        prop = new AppProperties(PROPERTIES_PATH);
        final String samplesDir = prop.getSamplesDir();
        final String cloudTopic = prop.getCloudTopic();

        // Retrieving the samples files names
        List<String> fileNames = getSamplesFiles(samplesDir, prop.getMaxSources());
        System.out.println(fileNames.size() + " files selected in '" + samplesDir + "'");

        // Build topics name (one topic per file)
        List<String> rfidTopics = new ArrayList<>(fileNames.size());
        for (int i = 0; i < fileNames.size(); i++) {
            String name = fileNames.get(i);
            rfidTopics.add(prop.getRootRfidTopic() + name);

            Path p = Paths.get(samplesDir, name);
            fileNames.set(i, p.toString());
        }

        // Start edge nodes (one edge node per file)
        System.out.print("Starting edge nodes... ");
        for (int i = 0; i < fileNames.size(); i++) {
            startEdgeNode("EDGE" + i, rfidTopics.get(i), cloudTopic);
        }
        System.out.println("done.");

        // Start one stub (only to check what will be sent on the cloudTopic)
        System.out.print("Starting a subscriber stub... ");
        startSubscriberStub("STUB", cloudTopic);
        System.out.println("done.");

        // Start data sources (one data source per file)
        System.out.print("Starting data sources... ");
        for (int i = 0; i < fileNames.size(); i++) {
            startDataSource("SOURCE" + i, rfidTopics.get(i), fileNames.get(i));
        }
        System.out.println("done.");
    }

    static List<String> getSamplesFiles(String samplesDir, int maxSampleSources) {
        // Read passed dir content
        File folder = new File(samplesDir);
        File[] ls = folder.listFiles();
        assert ls != null;

        // Filter directories and don't cross the passed max
        ArrayList<String> names = new ArrayList<>();
        for (File file: ls) {
            if (!file.isFile()) {
                continue;
            }

            names.add(file.getName());

            if (maxSampleSources > 0 && names.size() >= maxSampleSources) {
                break;
            }
        }
        return names;
    }

    static void startSubscriberStub(String clientId, String rfidTopic) throws Exception {
        // Debug purposes
        IMqttAsyncClient client = new MqttAsyncClient(prop.getBrokerHost(), clientId, new MemoryPersistence());
        AsyncSubscriber sub = new AsyncSubscriber(rfidTopic, prop.getQos(), client);
        sub.setCallback(payload -> {
            // Retrieve the truck id
            String msg = new String(payload);
            String truck = "TRUCK";
            int index = msg.indexOf(truck);
            truck += msg.charAt(index + truck.length());

            System.out.println("[CLOUD-" + truck + "] Received: \n" + msg);
        });

        sub.init();
    }

    static void startEdgeNode(String clientId, String rfidTopic, String cloudTopic) throws Exception {
        IMqttAsyncClient client = new MqttAsyncClient(prop.getBrokerHost(), clientId, new MemoryPersistence());
        AsyncPublisher pub = new AsyncPublisher(cloudTopic, prop.getQos(), client);
        AsyncSubscriber sub = new AsyncSubscriber(rfidTopic, prop.getQos(), client);

        // BulkBuilder to aggregate multiple payloads in one bulk
        BulkBuilder<String> bb =
                new StringBulkBuilder("\n");
                //new JsonBulkBuilder(new DisposalSampleFactory(prop.getFieldSeparator()));

        // Strategy on how to handle a sample arriving at the edge node
        TransmissionStrategy strategy =
                //new ImmediateTransmissionStrategy();
                new PeriodicTransmissionStrategy(LocalDateTime.now(), prop.getTransmissionPeriod(), prop.getEdgeBufferSize(), bb);

        new EdgeNode(pub, sub, strategy).start();
    }

    static void startDataSource(String clientId, String topic, String filePath) throws MqttException {
        IMqttAsyncClient client = new MqttAsyncClient(prop.getBrokerHost(), clientId, new MemoryPersistence());
        AsyncChannel pub = new AsyncPublisher(topic, prop.getQos(), client);
        new DataSourceSimulator(pub, filePath, prop.getRate()).start();
    }
}
