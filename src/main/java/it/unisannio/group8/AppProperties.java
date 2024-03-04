package it.unisannio.group8;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class AppProperties {
    private final String brokerHost;
    private final String fieldSeparator;
    private final int edgeBufferSize;
    private final int qos;
    private final float rate;
    private final long transmissionPeriod;
    private final int maxSources;
    private final String samplesDir;
    private final String rootRfidTopic;
    private final String cloudTopic;

    public AppProperties(String propertiesPath) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(propertiesPath));
        Properties prop = new Properties();
        prop.load(br);

        this.brokerHost = prop.getProperty("host.broker");
        this.qos = Integer.parseInt(prop.getProperty("qos"));
        this.rootRfidTopic = prop.getProperty("topic.rfid");
        this.cloudTopic = prop.getProperty("topic.cloud");
        this.samplesDir = prop.getProperty("samples.dir");
        this.fieldSeparator = prop.getProperty("samples.fields.separator");
        this.maxSources = Integer.parseInt(prop.getProperty("max.sources"));
        this.rate = Float.parseFloat(prop.getProperty("sources.rate"));
        this.edgeBufferSize = Integer.parseInt(prop.getProperty("edge.buffer.size"));
        this.transmissionPeriod = Long.parseLong(prop.getProperty("transmission.period"));
    }

    public String getBrokerHost() { return brokerHost; }
    public String getFieldSeparator() { return fieldSeparator; }
    public int getEdgeBufferSize() { return edgeBufferSize; }
    public int getQos() { return qos; }
    public float getRate() { return rate; }
    public long getTransmissionPeriod() { return transmissionPeriod; }
    public int getMaxSources() { return maxSources; }
    public String getSamplesDir() { return samplesDir; }
    public String getRootRfidTopic() { return rootRfidTopic; }
    public String getCloudTopic() { return cloudTopic; }
}