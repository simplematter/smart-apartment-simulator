# Smart apartment simulator

This service simulates 26 smart apartments with dozens of sensors sending data to an MQTT broker. 
Purpose of service is to provide realistically looking simulated data for IoT platform testing and demonstration.

Data is taken from [WSU Smart Apartment ADL Multi-Resident Testbed](http://casas.wsu.edu/datasets/) 
and maps 26 tests executed in different moments 
to 26 simulated smart apartments where activities happen at the same time during the day.

For credits and to know more about the testbeds, see [src/main/resources/adlmr/README](src/main/resources/adlmr/README).

Sensors are:
   M01..M51:  motion sensors
   I01..I08:  item sensors
   D01..D12:  cabinet sensor
   AD1-A:     water sensor
   AD1-B:     water sensor
   AD1-C:     burner sensor
   P01:       phone sensor (start/top is start/stop of phone call)
   T01..T03:  temperature sensors

Activity happened between 8.20 and 18.00 but when the simulation runs, the clock is set to 8.20, 
so data immediately is sent to the MQTT broker. Simulation user can change the start date 
with env variable to SIMULATION_START to shift the starting point of the simulation.

## Configuration

You can use the following environment variables for configuration:

- `MQTT_HOST`, `MQTT_PORT` - connection to the MQTT broker

- `MQTT_TOPIC_PREFIX` - prefix of MQTT topic. This will be followed by `/<senson id>` to construt the complete topic name

- `MQTT_QOS` - 0, 1 or 2 - QoS of the messages sent to MQTT broker.

- `SIMULATION_START` - by default `08:23:10`. Time that simulation reports when it starts. 

