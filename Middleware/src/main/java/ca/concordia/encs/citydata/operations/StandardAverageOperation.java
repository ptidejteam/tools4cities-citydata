package ca.concordia.encs.citydata.operations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import ca.concordia.encs.citydata.core.contracts.IOperation;
import ca.concordia.encs.citydata.core.contracts.IRunner;
import ca.concordia.encs.citydata.core.implementations.AbstractOperation;

public class StandardAverageOperation extends AbstractOperation<String> implements IOperation<String> {
	
	private String sensorIds;  // comma-separated sensor IDs
    private String date;
    private String startTime;  
    private String endTime;    
    private IRunner observer;

    public void setSensorIds(String sensorIds) {
        this.sensorIds = sensorIds;
    }

    public void setDate(String date) {
        this.date = date;
    }
    
    public void setStartTime(String startTime) {  // Add this
        this.startTime = startTime;
    }

    public void setEndTime(String endTime) {      // Add this
        this.endTime = endTime;
    }

    @Override
    public ArrayList<String> apply(ArrayList<String> input) {
        String[] targetSensors = sensorIds != null ? sensorIds.split(",") : new String[0];
        
        // Store humidity values per sensor using HashMap
        HashMap<String, ArrayList<Double>> sensorHumidityMap = new HashMap<>();

        for (String line : input) {
            if (line == null || line.trim().isEmpty()) continue;

            String[] cols = line.split(",");
            if (cols.length < 3) continue;

            String timestamp = cols[0].trim();
            String lineSensorId = cols[1].trim();
            String humidityValue = cols[2].trim();

            String lineDate = timestamp.length() >= 10 ? timestamp.substring(0, 10) : "";
            String lineTime = timestamp.length() >= 19 ? timestamp.substring(11, 19) : "";

            // Check if this sensor is in our target list
            boolean matchesSensor = false;
            for (String targetSensor : targetSensors) {
                if (lineSensorId.equals(targetSensor.trim())) {
                    matchesSensor = true;
                    break;
                }
            }

            boolean matchesDate = (date == null || date.isEmpty() || lineDate.equals(date));

         // Add time range check
            boolean withinTimeRange = true;
            if (startTime != null && !startTime.isEmpty() && endTime != null && !endTime.isEmpty()) {
                withinTimeRange = lineTime.compareTo(startTime) >= 0 && lineTime.compareTo(endTime) <= 0;
            }

            if (matchesSensor && matchesDate && withinTimeRange) {  // Update this condition
                try {
                    double humidity = Double.parseDouble(humidityValue);

                    if (!sensorHumidityMap.containsKey(lineSensorId)) {
                        sensorHumidityMap.put(lineSensorId, new ArrayList<>());
                    }
                    sensorHumidityMap.get(lineSensorId).add(humidity);

                } catch (NumberFormatException e) {
                    System.out.println("[DEBUG] Skipping non-numeric value: " + humidityValue);
                }
            }
        }

        // Calculate average for each sensor and build results
        ArrayList<String> result = new ArrayList<>();
        
        for (String targetSensor : targetSensors) {
            String sensor = targetSensor.trim();
            ArrayList<Double> values = sensorHumidityMap.get(sensor);
            
            if (values != null && !values.isEmpty()) {
                double sum = 0.0;
                for (double value : values) {
                    sum += value;
                }
                double average = sum / values.size();
                
                System.out.println("[DEBUG] Sensor " + sensor + ": Found " + values.size() + 
                                 " readings, Average: " + average);
                
                // Add each sensor result as a separate line
                result.add("sensor," + sensor + ",average_humidity," + average + 
                          ",date," + date + ",count," + values.size());
            } else {
                System.out.println("[DEBUG] Sensor " + sensor + ": No data found");
                result.add("sensor," + sensor + ",average_humidity,0.0,date," + date + ",count,0");
            }
        }

        System.out.println("[DEBUG] Returning " + result.size() + " sensor results");
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


}
