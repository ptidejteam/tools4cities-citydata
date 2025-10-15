package ca.concordia.encs.citydata.producers;

import java.nio.file.Paths;
import java.util.Map;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ca.concordia.encs.citydata.core.contracts.IProducer;
import ca.concordia.encs.citydata.core.exceptions.MiddlewareException;
import ca.concordia.encs.citydata.core.implementations.AbstractProducer;

public class OccupancyProducerMapped extends AbstractProducer<String> implements IProducer<String> {

	public OccupancyProducerMapped() {
        // Constructor
    }

    @Override
    public void fetch() {
        try {
            ByteArrayOutputStream outputStream = (ByteArrayOutputStream) this.fetchFromPath();

            String csvString = outputStream.toString(StandardCharsets.UTF_8);

            String[] lines = csvString.split("\t");

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

            System.out.println("[DEBUG] Loaded " + csvLines.size() + " occupancy records");

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
            throw new MiddlewareException.DatasetNotFound("Error processing occupancy CSV data: " + e.getMessage());
        }
    }


}
