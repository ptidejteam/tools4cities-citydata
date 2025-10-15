package ca.concordia.encs.citydata.operations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import ca.concordia.encs.citydata.core.contracts.IOperation;
import ca.concordia.encs.citydata.core.contracts.IRunner;
import ca.concordia.encs.citydata.core.implementations.AbstractOperation;

public class MultiSourceAggregationOperation extends AbstractOperation<String> implements IOperation<String> {
	private String sensorIds;
    private String date;
    private String room;
    private String desk;
    private IRunner observer;

    public void setSensorIds(String sensorIds) {
        this.sensorIds = sensorIds;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public void setDesk(String desk) {
        this.desk = desk;
    }

    @Override
    public ArrayList<String> apply(ArrayList<String> input) {
        String[] targetSensors = sensorIds != null ? sensorIds.split(",") : new String[0];
        
        // Store data per sensor
        HashMap<String, SensorAggregateData> sensorDataMap = new HashMap<>();

        for (String line : input) {
            if (line == null || line.trim().isEmpty()) continue;

            // Normalized format: sourceType,timestamp,sensor_id,value,room,desk
            String[] cols = line.split(",");
            if (cols.length < 6) continue;

            String sourceType = cols[0].trim();
            String timestamp = cols[1].trim();
            String lineSensorId = cols[2].trim();
            String value = cols[3].trim();
            String lineRoom = cols[4].trim();
            String lineDesk = cols[5].trim();

            String lineDate = timestamp.length() >= 10 ? timestamp.substring(0, 10) : "";

            // Check filters
            boolean matchesSensor = targetSensors.length == 0;
            if (!matchesSensor) {
                for (String targetSensor : targetSensors) {
                    if (lineSensorId.equals(targetSensor.trim())) {
                        matchesSensor = true;
                        break;
                    }
                }
            }

            boolean matchesDate = (date == null || date.isEmpty() || lineDate.equals(date));
            boolean matchesRoom = (room == null || room.isEmpty() || lineRoom.equals(room));
            boolean matchesDesk = (desk == null || desk.isEmpty() || lineDesk.contains(desk));

            if (matchesSensor && matchesDate && matchesRoom && matchesDesk) {
                if (!sensorDataMap.containsKey(lineSensorId)) {
                    sensorDataMap.put(lineSensorId, new SensorAggregateData());
                }
                
                SensorAggregateData data = sensorDataMap.get(lineSensorId);
                
                try {
                    double numValue = Double.parseDouble(value);
                    
                    if (sourceType.equals("TEMPERATURE")) {
                        data.addTemperature(numValue);
                    } else if (sourceType.equals("HUMIDITY")) {
                        data.addHumidity(numValue);
                    } else if (sourceType.equals("OCCUPANCY")) {
                        data.addOccupancy(numValue);
                    }
                } catch (NumberFormatException e) {
                    // Skip non-numeric values
                }
            }
        }

        // Build results
        ArrayList<String> result = new ArrayList<>();
        
        for (Map.Entry<String, SensorAggregateData> entry : sensorDataMap.entrySet()) {
            String sensorId = entry.getKey();
            SensorAggregateData data = entry.getValue();
            
            System.out.println("[DEBUG] Sensor " + sensorId + ": " + data.toString());
            
            result.add("sensor," + sensorId + 
                      ",avg_temp," + data.getAvgTemperature() + 
                      ",avg_humidity," + data.getAvgHumidity() + 
                      ",occupancy_count," + data.getOccupancyCount() +
                      ",room," + room + 
                      ",date," + date);
        }

        return result;
    }

    @Override
    public void addObserver(IRunner aRunner) {
        this.observer = aRunner;
    }

    @Override
    public void notifyObservers() {
        if (observer != null) {
            observer.newOperationApplied(this);
        }
    }

    // Helper class to store aggregated sensor data
    private static class SensorAggregateData {
        private ArrayList<Double> temperatures = new ArrayList<>();
        private ArrayList<Double> humidities = new ArrayList<>();
        private int occupancyCount = 0;
        
        public void addTemperature(double temp) {
            temperatures.add(temp);
        }
        
        public void addHumidity(double humidity) {
            humidities.add(humidity);
        }
        
        public void addOccupancy(double occupancy) {
            if (occupancy == 1.0) {
                occupancyCount++;
            }
        }
        
        public double getAvgTemperature() {
            if (temperatures.isEmpty()) return 0.0;
            double sum = 0.0;
            for (double temp : temperatures) sum += temp;
            return sum / temperatures.size();
        }
        
        public double getAvgHumidity() {
            if (humidities.isEmpty()) return 0.0;
            double sum = 0.0;
            for (double hum : humidities) sum += hum;
            return sum / humidities.size();
        }
        
        public int getOccupancyCount() {
            return occupancyCount;
        }
        
        @Override
        public String toString() {
            return "Temp=" + getAvgTemperature() + ", Humidity=" + getAvgHumidity() + 
                   ", Occupancy=" + occupancyCount;
        }
    }
}
