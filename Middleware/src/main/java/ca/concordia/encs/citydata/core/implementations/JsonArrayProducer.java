package ca.concordia.encs.citydata.core.implementations;

import java.io.OutputStream;
import java.util.ArrayList;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import ca.concordia.encs.citydata.core.contracts.IProducer;
import ca.concordia.encs.citydata.core.utils.RequestOptions;

/**
*This acts as a base producer for EnergyConsuimption Producer and any other producer that needs to return a JSON array.
*
* @author Sikandar Ejaz
* @since 2026-05-13
*/

public non-sealed class JsonArrayProducer extends AbstractProducer<JsonArray> implements IProducer<JsonArray> {

	public JsonArrayProducer(final String filePath) {
		super(filePath);
	}

	public JsonArrayProducer(final String filePath, final RequestOptions fileOptions) {
		super(filePath, fileOptions);
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
