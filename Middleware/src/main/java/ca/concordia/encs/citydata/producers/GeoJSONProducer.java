package ca.concordia.encs.citydata.producers;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import ca.concordia.encs.citydata.core.contracts.IProducer;
import ca.concordia.encs.citydata.core.implementations.AbstractProducer;

/**
 * This Producer loads a GeoJSON file and outputs its content as a JsonObject.
 * Uses Spring's PathMatchingResourcePatternResolver with the thread context
 * classloader, which correctly resolves BOOT-INF/classes/ inside a Spring Boot
 * JAR, as well as falling back to the filesystem for external files on the server.
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

		// Use thread context classloader — correctly resolves BOOT-INF/classes/ in Spring Boot JAR
		PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(
				Thread.currentThread().getContextClassLoader());

		InputStream inputStream = null;

		// 1. Try classpath first (works inside JAR / Docker)
		try {
			Resource classpathResource = resolver.getResource("classpath:" + filePath);
			if (classpathResource.exists()) {
				inputStream = classpathResource.getInputStream();
			}
		} catch (Exception ignored) {
		}

		// 2. Fall back to filesystem (works on server with external files)
		if (inputStream == null) {
			try {
				Resource fileResource = resolver.getResource("file:" + filePath);
				if (fileResource.exists()) {
					inputStream = fileResource.getInputStream();
				}
			} catch (Exception ignored) {
			}
		}

		if (inputStream == null) {
			throw new RuntimeException("GeoJSON file not found on classpath or filesystem: " + filePath);
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