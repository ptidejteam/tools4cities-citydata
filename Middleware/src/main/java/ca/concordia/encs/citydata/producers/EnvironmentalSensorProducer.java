package ca.concordia.encs.citydata.producers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import ca.concordia.encs.citydata.core.contracts.IProducer;
import ca.concordia.encs.citydata.core.exceptions.MiddlewareException;
import ca.concordia.encs.citydata.core.implementations.AbstractProducer;
import ca.concordia.encs.citydata.core.utils.RequestOptions;

public class EnvironmentalSensorProducer extends AbstractProducer<String> implements IProducer<String> {
	
	public EnvironmentalSensorProducer() {
 
    }
    
    @Override
    public void fetch() {
        try {
        	
            ByteArrayOutputStream outputStream = (ByteArrayOutputStream) this.fetchFromPath();

            String csvString = outputStream.toString(StandardCharsets.UTF_8);

            String[] lines = csvString.split("\\R");

            if (lines.length > 0) {
                System.out.println("[DEBUG] Header: " + lines[0]);
            }

            ArrayList<String> csvLines = new ArrayList<>();
            for (int i = 1; i < lines.length; i++) {
                String line = lines[i].trim();
                if (!line.isEmpty()) {
                    csvLines.add(line);
                }
            }

            for (int i = 0; i < Math.min(3, csvLines.size()); i++) {
                System.out.println("[DEBUG] Line " + i + ": " + csvLines.get(i));
            }

            this.setResult(csvLines);
            this.applyOperation();

            if (this.getOperation() == null) {
                System.out.println("[DEBUG] No operation applied. Printing all data:");
                for (String line : csvLines) {
                    System.out.println(line);
                }
            }
            
        } catch (Exception e) {
            throw new MiddlewareException.DatasetNotFound("Error processing temperature CSV data: " + e.getMessage());
        }
    }
}