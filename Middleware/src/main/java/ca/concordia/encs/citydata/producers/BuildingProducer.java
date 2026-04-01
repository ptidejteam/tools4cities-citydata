package ca.concordia.encs.citydata.producers;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidParameterException;
import java.util.ArrayList;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import ca.concordia.encs.citydata.core.contracts.IOperation;
import ca.concordia.encs.citydata.core.contracts.IRunner;
import ca.concordia.encs.citydata.core.implementations.JSONProducer;
import ca.concordia.encs.citydata.core.utils.RequestOptions;

/**
 * This Producer outputs metadata about a building, such as floors, zones and sensors.
 * @author Gabriel C. Ullmann
 * @since 2025-05-28
 */

public final class BuildingProducer extends JSONProducer {

	public BuildingProducer(String filePath, RequestOptions fileOptions) {
		super(filePath, fileOptions);
		// TODO Auto-generated constructor stub
	}

	public BuildingProducer() {
		// TODO Auto-generated constructor stub
	}

	private String filePath;

	public void setBuildingName(String buildingName) {
		if (buildingName != null) {
			this.filePath = "./src/test/resources/" + buildingName + "_building.json";
		} else {
			throw new InvalidParameterException("Please provide a building name to the producer.");
		}
	}

	@Override
	public void fetch() {
		InputStream inputStream = null;

		// 1. Try classpath first
		inputStream = getClass().getClassLoader().getResourceAsStream(filePath);

		// 2. Fall back to filesystem
		if (inputStream == null) {
			try {
				inputStream = Files.newInputStream(Paths.get(filePath));
			} catch (Exception e) {
				throw new RuntimeException("Building file not found on classpath or filesystem: " + filePath, e);
			}
		}

		final ArrayList<JsonObject> jsonOutput = new ArrayList<>();

		try (InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
			final JsonElement parsedElement = JsonParser.parseReader(reader);

			JsonObject outputJsonObject = new JsonObject();
			if (parsedElement.isJsonArray()) {
				outputJsonObject.add("result", parsedElement.getAsJsonArray());
			} else {
				outputJsonObject = parsedElement.getAsJsonObject();
			}

			jsonOutput.add(outputJsonObject);
		} catch (Exception e) {
			throw new RuntimeException("Failed to read building file: " + filePath, e);
		}

		this.setResult(jsonOutput);
		this.applyOperation();
	}

	@Override
	public void addObserver(IRunner aRunner) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setOperation(IOperation operation) {
		// TODO Auto-generated method stub

	}

	@Override
	public void applyOperation() {
		// TODO Auto-generated method stub

	}

	@Override
	public void notifyObservers() {
		// TODO Auto-generated method stub

	}

	@Override
	public ArrayList<JsonObject> getResult() {
		// TODO Auto-generated method stub
		return null;
	}
}
