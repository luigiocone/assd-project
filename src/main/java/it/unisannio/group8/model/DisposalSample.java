package it.unisannio.group8.model;

public class DisposalSample {
    private String bagId;         // Garbage bag id on its RFID chip
    private String timestamp;
    private String truckId;       // License plate
    private int occurrences;      // Truck reads the same bag id multiple times
    private float latitude;
    private float longitude;

    public DisposalSample(String bagId, String timestamp, String truckId, int occurrences, float latitude, float longitude) {
        this.bagId = bagId;
        this.timestamp = timestamp;
        this.truckId = truckId;
        this.occurrences = occurrences;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getBagId() { return bagId; }
    public String getTimestamp() { return timestamp; }
    public String getTruckId() { return truckId; }
    public int getOccurrences() { return occurrences; }
    public float getLatitude() { return latitude; }
    public float getLongitude() { return longitude; }

    public void setBagId(String bagId) { this.bagId = bagId; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    public void setTruckId(String truckId) { this.truckId = truckId; }
    public void setOccurrences(int occurrences) { this.occurrences = occurrences; }
    public void setLatitude(float latitude) { this.latitude = latitude; }
    public void setLongitude(float longitude) { this.longitude = longitude; }
}
