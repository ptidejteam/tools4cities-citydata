package ca.concordia.encs.citydata.producers;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import org.springframework.security.core.context.SecurityContextHolder;

import ca.concordia.encs.citydata.core.exceptions.AccessDeniedException;
import ca.concordia.encs.citydata.core.exceptions.MiddlewareException;
import ca.concordia.encs.citydata.core.implementations.CSVProducer;
import ca.concordia.encs.citydata.core.utils.RequestOptions;
import ca.concordia.encs.citydata.services.DatasetAccessService;

/**
 * This producer reads Flow data from a CSV source, processes it line by line, and produces a result set for the data 
 * readings for further operations. It stores all non-empty lines, optionally applies a configured operation on the data, and 
 * makes the processed results available to consumers.
 * @author Peter Yefi, Vinicius Mioto, Tahereh Bijani,  Mohamed Jendoubi  
 * @date: 2026-06-27
 * @author: Minette Z. Fixed the producer by adding the required constructor from CSVProducer to properly initialize the inherited base producer (CSVProducer)
 * @date: 2026-05-29
 */

public class BTUProducer extends CSVProducer {

	private String metadataPath;

	public BTUProducer(String filePath) {
		super(filePath);
	}

	public BTUProducer(final String filePath, final RequestOptions fileOptions) {
		super(filePath, fileOptions);
	}

	public void setMetadataPath(String metadataPath) {
		this.metadataPath = metadataPath;
	}

	@Override
	public void fetch() {
		try {
			if (metadataPath != null) {
				String username = SecurityContextHolder.getContext().getAuthentication().getName();
				new DatasetAccessService().checkAuthorisationForPath(username, metadataPath);
			}

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

		} catch (AccessDeniedException e) {
			throw e;
		} catch (Exception e) {
			throw new MiddlewareException.DatasetNotFound("Error processing BTU CSV data: " + e.getMessage());
		}
	}
}