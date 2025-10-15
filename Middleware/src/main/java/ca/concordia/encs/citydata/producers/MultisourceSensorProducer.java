package ca.concordia.encs.citydata.producers;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import ca.concordia.encs.citydata.core.contracts.IProducer;
import ca.concordia.encs.citydata.core.exceptions.MiddlewareException;
import ca.concordia.encs.citydata.core.implementations.AbstractProducer;
import ca.concordia.encs.citydata.core.utils.RequestOptions;

public class MultisourceSensorProducer extends AbstractProducer<String> implements IProducer<String> {

	private String temperatureFilePath;
    private String humidityFilePath;
    private String occupancyFilePath;

    public void setTemperatureFilePath(String path) {
        this.temperatureFilePath = path;
    }

    public void setHumidityFilePath(String path) {
        this.humidityFilePath = path;
    }

    public void setOccupancyFilePath(String path) {
        this.occupancyFilePath = path;
    }

    @Override
    public void fetch() {
        try {
            ArrayList<String> allData = new ArrayList<>();

            // Read temperature file
            if (temperatureFilePath != null && !temperatureFilePath.isEmpty()) {
                allData.addAll(readTemperatureHumidityFile(temperatureFilePath, "TEMPERATURE"));
            }

            // Read humidity file
            if (humidityFilePath != null && !humidityFilePath.isEmpty()) {
                allData.addAll(readTemperatureHumidityFile(humidityFilePath, "HUMIDITY"));
            }

            // Read occupancy file
            if (occupancyFilePath != null && !occupancyFilePath.isEmpty()) {
                allData.addAll(readOccupancyFile(occupancyFilePath));
            }

            System.out.println("[DEBUG] Loaded total " + allData.size() + " records from all sources");

            this.setResult(allData);
            this.applyOperation();

        } catch (Exception e) {
            throw new MiddlewareException.DatasetNotFound("Error processing multi-source data: " + e.getMessage());
        }
    }

    private ArrayList<String> readTemperatureHumidityFile(String filePath, String sourceType) {
        ArrayList<String> lines = new ArrayList<>();
        
        this.setFilePath(filePath);
        ByteArrayOutputStream outputStream = (ByteArrayOutputStream) this.fetchFromPath();
        String csvString = outputStream.toString(StandardCharsets.UTF_8);
        String[] fileLines = csvString.split("\\R");

        // Skip header (line 0)
        for (int i = 1; i < fileLines.length; i++) {
            String line = fileLines[i].trim();
            if (line.isEmpty()) continue;

            String[] cols = line.split(",");
            if (cols.length < 6) continue;

            // Normalize: sourceType,timestamp,sensor_id,value,room,desk
            String normalized = sourceType + "," + 
                              cols[0].trim() + "," +  // timestamp
                              cols[1].trim() + "," +  // sensor_id
                              cols[2].trim() + "," +  // value
                              cols[5].trim() + "," +  // room
                              "N/A";                   // desk (not available)
            
            lines.add(normalized);
        }

        System.out.println("[DEBUG] Loaded " + lines.size() + " lines from " + sourceType);
        return lines;
    }

    private ArrayList<String> readOccupancyFile(String filePath) {
        ArrayList<String> lines = new ArrayList<>();
        
        this.setFilePath(filePath);
        ByteArrayOutputStream outputStream = (ByteArrayOutputStream) this.fetchFromPath();
        String csvString = outputStream.toString(StandardCharsets.UTF_8);
        String[] fileLines = csvString.split("\\R");

        // Skip header (line 0)
        for (int i = 1; i < fileLines.length; i++) {
            String line = fileLines[i].trim();
            if (line.isEmpty()) continue;

            String[] cols = line.split(",");
            if (cols.length < 7) continue;

            // Normalize: sourceType,timestamp,sensor_id,value,room,desk
            String normalized = "OCCUPANCY," + 
                              cols[0].trim() + "," +  // timestamp
                              cols[1].trim() + "," +  // sensor_id
                              cols[2].trim() + "," +  // occupancy_value
                              cols[5].trim() + "," +  // room
                              cols[6].trim();          // desk (e.g., desk/411-3)
            
            lines.add(normalized);
        }

        System.out.println("[DEBUG] Loaded " + lines.size() + " lines from OCCUPANCY");
        return lines;
    }

}
