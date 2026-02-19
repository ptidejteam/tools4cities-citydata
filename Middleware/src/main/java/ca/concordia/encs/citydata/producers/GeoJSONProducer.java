package ca.concordia.encs.citydata.producers;

import ca.concordia.encs.citydata.core.contracts.IOperation;
import ca.concordia.encs.citydata.core.contracts.IProducer;
import ca.concordia.encs.citydata.core.contracts.IRunner;
import ca.concordia.encs.citydata.core.implementations.AbstractProducer;
import ca.concordia.encs.citydata.producers.base.JSONProducer;

/**
 * This Producer outputs metadata about a building. 
 * @author Sikandar Ejaz
 * @since 2026-02-16
 */

public class GeoJSONProducer extends AbstractProducer<com.google.gson.JsonObject>
		implements IProducer<com.google.gson.JsonObject> {
	private JSONProducer jsonProducer;

	public void setFilePath(String filePath) {
		if (filePath != null) {
			jsonProducer = new JSONProducer(filePath, null);
		} else {
			throw new java.security.InvalidParameterException("Please provide a file path to the producer.");
		}
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void setOperation(IOperation operation) {
		this.jsonProducer.setOperation(operation);
	}

	@Override
	public void fetch() {
		this.jsonProducer.fetch();
	}

	@Override
	public void addObserver(final IRunner aRunner) {
		this.jsonProducer.addObserver(aRunner);
	}
}
