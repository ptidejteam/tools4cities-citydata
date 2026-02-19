package ca.concordia.encs.citydata.producers;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import ca.concordia.encs.citydata.core.contracts.IProducer;
import ca.concordia.encs.citydata.core.implementations.AbstractProducer;

/**
 * This Producer loads a GeoJSON file and outputs its content as a JsonObject.
 * It first attempts to load from the classpath (works inside JAR/Docker),
 * then falls back to the filesystem (works on server with external files).
 *
 * @author Sikandar Ejaz
 * @since 2026-02-16
 */
public class GeoJSONProducer extends AbstractProducer<JsonObject> implements IProducer<JsonObject> {

	@Override
	public void fetch() {
		String filePath = this.getFilePath();

		if (filePath == null || filePath.isEmpty()) {
			throw new RuntimeException("Please provide a file path to the producer.");
		}

		InputStream inputStream = null;

		// 1. Try classpath first (works inside JAR / Docker)
		inputStream = getClass().getClassLoader().getResourceAsStream(filePath);

		// 2. Fall back to filesystem (works on server with external files)
		if (inputStream == null) {
			try {
				inputStream = Files.newInputStream(Paths.get(filePath));
			} catch (Exception e) {
				throw new RuntimeException("GeoJSON file not found on classpath or filesystem: " + filePath, e);
			}
		}

		final ArrayList<JsonObject> jsonOutput = new ArrayList<>();

		try (InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
			JsonElement parsedElement = JsonParser.parseReader(reader);

			JsonObject outputJsonObject = new JsonObject();
			if (parsedElement.isJsonArray()) {
				outputJsonObject.add("result", parsedElement.getAsJsonArray());
			} else {
				outputJsonObject = parsedElement.getAsJsonObject();
			}

			jsonOutput.add(outputJsonObject);

		} catch (Exception e) {
			throw new RuntimeException("Failed to read GeoJSON file: " + filePath, e);
		}

		this.setResult(jsonOutput);
		this.applyOperation();
	}
}