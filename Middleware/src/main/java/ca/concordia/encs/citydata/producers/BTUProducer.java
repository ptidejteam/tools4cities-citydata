package ca.concordia.encs.citydata.producers;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import ca.concordia.encs.citydata.core.exceptions.MiddlewareException;
import ca.concordia.encs.citydata.core.implementations.CSVProducer;


/**
 * This producer reads Flow data from a CSV source, processes it line by line, and produces a result set for the data 
 * readings for further operations. It stores all non-empty lines, optionally applies a configured operation on the data, and 
 * makes the processed results available to consumers.
 * @author Peter Yefi, Vinicius Mioto, Tahereh Bijani,  Mohamed Jendoubi  
 * @date: 2026-06-27
 */
public class BTUProducer extends CSVProducer{

	public BTUProducer(String filePath) {
		super(filePath);
	}
	
	@Override
	public void fetch() {
		try {

			ByteArrayOutputStream outputStream = (ByteArrayOutputStream) this.fetchFromPath();

			String csvString = outputStream.toString(StandardCharsets.UTF_8);

			String[] lines = csvString.split("\\R");

			ArrayList<String> csvLines = new ArrayList<>();
			for (int i = 1; i < lines.length; i++) {
				String line = lines[i].trim();
				if (!line.isEmpty()) {
					csvLines.add(line);
				}
			}

			this.setResult(csvLines);
			this.applyOperation();


		} catch (Exception e) {
			throw new MiddlewareException.DatasetNotFound("Error processing BTU CSV data: " + e.getMessage());
		}
	}

}
