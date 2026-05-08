package ca.concordia.encs.citydata.core.implementation;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;

import ca.concordia.encs.citydata.core.contract.IProducer;
import ca.concordia.encs.citydata.core.exception.MiddlewareException;
import ca.concordia.encs.citydata.core.util.RequestOptions;

/**
 * This producer can load CSV from a file or remotely via an HTTP request.
 *
 * @author Gabriel C. Ullmann
 * @since 2024-12-01
 */

public non-sealed class CSVProducer extends AbstractProducer<String> implements IProducer<String> {

	public CSVProducer(final String filePath, final RequestOptions fileOptions) {
		super(filePath, fileOptions);
	}

	public CSVProducer(final String filePath) {
		super(filePath);
	}

	@Override
	public void fetch() {
		try (OutputStream outputStream = this.fetchFromPath();
				OutputStreamWriter writer = new OutputStreamWriter(outputStream)) {
			writer.flush(); // Ensure all data is written to the stream
			String csvString = outputStream.toString();

			// Split CSV string by line, add lines to the list
			ArrayList<String> csvLines = new ArrayList<>();
			csvLines.addAll(Arrays.asList(csvString.split(System.lineSeparator())));
			this.setResult(csvLines);
			this.applyOperation();
		} catch (IOException e) {
			throw new MiddlewareException.DatasetNotFound("Error processing CSV data");
		}
	}
}