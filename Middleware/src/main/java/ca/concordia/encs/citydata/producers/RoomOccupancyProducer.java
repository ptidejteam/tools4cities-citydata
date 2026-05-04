package ca.concordia.encs.citydata.producers;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import ca.concordia.encs.citydata.core.exceptions.MiddlewareException;
import ca.concordia.encs.citydata.core.implementations.CSVProducer;

/**
 * This producer reads an sensor data from a CSV file, extracts all data lines, and provides them as input to the potential further 
 * operations
 * @author Minette Zongo M., Sikandar Ejaz
 * @date: 2025-10-04
 */

public class RoomOccupancyProducer extends CSVProducer {

	public RoomOccupancyProducer(String filePath) {
		super(filePath);
	}

	@Override
	public void fetch() {
		try {
			ByteArrayOutputStream outputStream = (ByteArrayOutputStream) this.fetchFromPath();
			String csvString = outputStream.toString(StandardCharsets.UTF_8);

			System.out.println(csvString.substring(0, Math.min(500, csvString.length())));

			// Split by newlines
			String[] lines = csvString.split("\\R");

			ArrayList<String> csvLines = new ArrayList<>();

			// Skip header (line 0) and process data lines
			for (int i = 1; i < lines.length; i++) {
				String line = lines[i].trim();
				if (!line.isEmpty()) {
					csvLines.add(line);
				}
			}

			this.setResult(csvLines);

			this.applyOperation();

		} catch (Exception e) {
			e.printStackTrace();
			throw new MiddlewareException.DatasetNotFound("Error processing occupancy CSV data: " + e.getMessage());
		}
	}
}
