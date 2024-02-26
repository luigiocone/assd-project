# assd-project
Unisannio project for "Architetture e sistemi software distribuiti"
# Usage
Run an MQTT broker (supposing artemis in the following)
```
# Create a broker allowing non authenticated connections (only once)
artemis-dir/bin/artemis create --allow-anonymous assd-broker

# Run the broker
artemis-dir/bin/artemis/assd-broker/bin/artemis run
```

Run the application
```
mvn compile
mvn exec:java
```
