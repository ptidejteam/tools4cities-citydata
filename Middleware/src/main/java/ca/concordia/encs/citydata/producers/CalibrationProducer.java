package ca.concordia.encs.citydata.producers;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;

import ca.concordia.encs.citydata.core.exceptions.MiddlewareException;
import ca.concordia.encs.citydata.core.implementations.CSVProducer;

//public class CalibrationProducer extends AbstractProducer<String> implements IProducer<String> {
public class CalibrationProducer extends CSVProducer {

	/*	public void setFilePath(String fileName) {
			super.setFilePath("docs/examples/data/" + fileName);
		}*/

	public CalibrationProducer(String filePath) {
		super(filePath);
	}

	@Override
	public void fetch() {
		System.out.println("Fetching file from path: " + this.getFilePath());
		try {
			OutputStream outputStream = this.fetchFromPath();
			String csvString = outputStream.toString();
			ArrayList<String> csvLines = new ArrayList<>(Arrays.asList(csvString.split(System.lineSeparator())));
			this.setResult(csvLines);
			this.applyOperation();
		} catch (Exception e) {
			throw new MiddlewareException.DatasetNotFound("Error processing CSV data: " + e.getMessage());
		}
	}
}
