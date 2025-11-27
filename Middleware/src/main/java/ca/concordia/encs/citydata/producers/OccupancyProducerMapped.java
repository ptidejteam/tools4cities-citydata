package ca.concordia.encs.citydata.producers;

import java.nio.file.Paths;

import java.util.Map;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import ca.concordia.encs.citydata.core.contracts.IProducer;
import ca.concordia.encs.citydata.core.exceptions.MiddlewareException;
import ca.concordia.encs.citydata.core.implementations.AbstractProducer;

/**
 * This producer reads an sensor data from a CSV file, extracts all data lines, and provides them as input to the potential further 
 * operations
 * @author Minette Zongo M.
 * @date: 2025-10-04
 */

public class OccupancyProducerMapped extends AbstractProducer<String> implements IProducer<String> {

    public OccupancyProducerMapped() {
        // Constructor
    }

    @Override
    public void fetch() {
        try {
            ByteArrayOutputStream outputStream = (ByteArrayOutputStream) this.fetchFromPath();
            String csvString = outputStream.toString(StandardCharsets.UTF_8);

            // Debug: Check raw content
            System.out.println("[DEBUG] Raw CSV first 500 chars:");
            System.out.println(csvString.substring(0, Math.min(500, csvString.length())));
            System.out.println("[DEBUG] ---");

            // Split by newlines
            String[] lines = csvString.split("\\R");

            System.out.println("[DEBUG] Total lines found: " + lines.length);
            
            if (lines.length > 0) {
                System.out.println("[DEBUG] First line (header): " + lines[0]);
            }
            if (lines.length > 1) {
                System.out.println("[DEBUG] Second line (data): " + lines[1]);
            }

            ArrayList<String> csvLines = new ArrayList<>();

            // Skip header (line 0) and process data lines
            for (int i = 1; i < lines.length; i++) {
                String line = lines[i].trim();
                if (!line.isEmpty()) {
                    csvLines.add(line);
                    
                    // Debug first few lines
                    if (i <= 3) {
                        System.out.println("[DEBUG] Added line " + i + " (length=" + line.length() + "): " + line.substring(0, Math.min(100, line.length())));
                    }
                }
            }

            System.out.println("[DEBUG] Total data lines added to csvLines: " + csvLines.size());

            // CRITICAL: Make sure result is set BEFORE applying operation
            this.setResult(csvLines);
            
            System.out.println("[DEBUG] Result set, now applying operation...");
            
            this.applyOperation();

        } catch (Exception e) {
            e.printStackTrace();
            throw new MiddlewareException.DatasetNotFound("Error processing occupancy CSV data: " + e.getMessage());
        }
    }
}
