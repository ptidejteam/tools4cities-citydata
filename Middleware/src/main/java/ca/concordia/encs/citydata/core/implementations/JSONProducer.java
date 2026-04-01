package ca.concordia.encs.citydata.core.implementations;

import java.io.OutputStream;
import java.util.ArrayList;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import ca.concordia.encs.citydata.core.contracts.IProducer;
import ca.concordia.encs.citydata.core.utils.RequestOptions;

/**
 * This producer can load JSON from a file or remotely via an HTTP request.
 *
 * @author Gabriel C. Ullmann
 * @since 2024-12-01
 */

public non-sealed class JSONProducer extends AbstractProducer<JsonObject> implements IProducer<JsonObject> {

	public JSONProducer(String filePath, RequestOptions fileOptions) {
		this.setFilePath(filePath);
		this.setFileOptions(fileOptions);
	}

	public JSONProducer() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void fetch() {
		final ArrayList<JsonObject> jsonOutput = new ArrayList<>();

		// Use ByteArrayOutputStream to fetch data

		OutputStream outputStream = this.fetchFromPath();
		String inputJson = outputStream.toString();

		// Convert JSON string to object
		final JsonElement inputJsonElement = JsonParser.parseString(inputJson);

		JsonObject outputJsonObject = new JsonObject();
		if (inputJsonElement.isJsonArray()) {
			outputJsonObject.add("result", inputJsonElement);
		} else {
			outputJsonObject = inputJsonElement.getAsJsonObject();
		}

		jsonOutput.add(outputJsonObject);
		this.setResult(jsonOutput);
		this.applyOperation();
	}
}