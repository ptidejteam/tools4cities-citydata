package ca.concordia.encs.citydata.core.implementation;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import com.google.gson.JsonObject;

import ca.concordia.encs.citydata.core.config.PortfolioManagerConfig;
import ca.concordia.encs.citydata.core.contract.IProducer;
import ca.concordia.encs.citydata.core.util.RequestOptions;
import ca.concordia.encs.citydata.datastore.InMemoryDataStore;
import ca.concordia.encs.citydata.runner.SingleStepRunner;

/**
 * Producer that fetches meter consumption data from the ENERGY STAR Portfolio Manager API, transforms the XML response,
 * and forwards structured results to the CITYdata for potential operations.
 *
 * @author Minette Zongo
 * @since 2026-02-24
 */

//Need to discuss with Yann, and then probably move this back to producers package

public non-sealed class PortfolioManagerProducer extends AbstractProducer<JsonObject> implements IProducer<JsonObject> {
	private String meterId;
	private final ArrayList<JsonObject> intermediateResult = new ArrayList<>();

	public void setMeterId(String meterId) {
		this.meterId = meterId;
	}

	@SuppressWarnings("unchecked")
	private ArrayList<JsonObject> getMeterMetadata(SingleStepRunner runner) {
		final InMemoryDataStore store = InMemoryDataStore.getInstance();
		final String runnerId = runner.getMetadata("id").toString();
		final IProducer<?> storeResult = store.get(runnerId);
		if (storeResult != null) {
			return (ArrayList<JsonObject>) storeResult.getResult();
		}
		return new ArrayList<>();
	}

	private void validateMeterMetadata(ArrayList<JsonObject> metadataObject) {
		if (metadataObject.isEmpty()) {
			throw new RuntimeException(
					"Meter metadata is empty. " + "Check that meterId '" + this.meterId + "' is valid.");
		}
		final JsonObject wrapper = metadataObject.getFirst();
		final String xml = wrapper.get("xml").getAsString();
		if (xml.contains("status=\"Error\"")) {
			throw new RuntimeException("Portfolio Manager returned an error for meter " + this.meterId + ": " + xml);
		}
	}

	private String fetchConsumptionData() {
		try {

			final PortfolioManagerMetadataProducer metadataProducer = new PortfolioManagerMetadataProducer();
			metadataProducer.setMeterId(this.meterId);
			metadataProducer.setDataType("METER");

			final SingleStepRunner internalRunner = new SingleStepRunner(metadataProducer);
			final Thread runnerTask = new Thread(() -> {
				try {
					internalRunner.runSteps();
					while (!internalRunner.isDone()) {
						System.out.println("Busy waiting for meter metadata!");
					}
				} catch (Exception e) {
					internalRunner.setAsDone();
					System.out.println(e.getMessage());
				}
			});
			runnerTask.start();
			runnerTask.join();

			final ArrayList<JsonObject> meterMetadata = getMeterMetadata(internalRunner);
			validateMeterMetadata(meterMetadata);

			final String consumptionEndpoint = PortfolioManagerConfig.getBaseUrl() + "/meter/" + this.meterId
					+ "/consumptionData";
			final RequestOptions requestOptions = new RequestOptions();
			requestOptions.setMethod("GET");
			requestOptions.addToHeaders("Authorization", buildBasicAuth());
			requestOptions.addToHeaders("Accept", "application/xml");

			this.setFilePath(consumptionEndpoint);
			this.setFileOptions(requestOptions);
			return this.fetchFromPath().toString();

		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new RuntimeException("Interrupted while fetching meter metadata: " + e.getMessage(), e);
		}
	}

	@Override
	public void fetch() {
		if (this.meterId == null) {
			throw new RuntimeException(
					"'meterId' is required. " + "Use PortfolioManagerMetadataProducer with dataType=METER_LIST "
							+ "to discover available meter IDs for your property.");
		}

		final String xmlResult = fetchConsumptionData();

		final JsonObject wrapper = new JsonObject();
		wrapper.addProperty("xml", xmlResult);
		wrapper.addProperty("meterId", this.meterId);

		this.intermediateResult.add(wrapper);
		this.setResult(this.intermediateResult);
		this.applyOperation();
	}

	private String buildBasicAuth() {
		return "Basic " + java.util.Base64.getEncoder()
				.encodeToString((PortfolioManagerConfig.getUsername() + ":" + PortfolioManagerConfig.getPassword())
						.getBytes(StandardCharsets.UTF_8));
	}

}
