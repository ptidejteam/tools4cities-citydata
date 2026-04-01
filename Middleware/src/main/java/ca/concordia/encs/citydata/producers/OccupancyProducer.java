package ca.concordia.encs.citydata.producers;

import java.util.ArrayList;
import java.util.Random;

import ca.concordia.encs.citydata.core.implementations.CSVProducer;
import ca.concordia.encs.citydata.core.utils.RequestOptions;

/**
 *
 * This Producer simulates an occupancy sensor.
 *
 * @author Sikandar Ejaz, Gabriel C. Ullmann
 * @since 2025-05-28
 */

public final class OccupancyProducer extends CSVProducer {
	public OccupancyProducer(String filePath, RequestOptions fileOptions) {
		super(filePath, fileOptions);
		// TODO Auto-generated constructor stub
	}

	private int listSize;

	public void setListSize(int listSize) {
		this.listSize = listSize;
	}

	@Override
	public void fetch() {
		int changeCount = 0;
		var previousData = "";
		final Random random = new Random();
		// if this is running for the first time, fetch
		// otherwise, just apply next operation on top of previous result
		if (this.isEmpty()) {
			final ArrayList<String> randomOccupancy = new ArrayList<String>();
			for (int i = 0; i < this.listSize; i++) {
				String occupancyValue = random.nextBoolean() ? "Occupied" : "Vacant";
				randomOccupancy.add(occupancyValue);
				if (!previousData.equals(occupancyValue)) {
					changeCount++;
					System.out.println("Change: " + changeCount);
				}
				previousData = occupancyValue;
			}
			this.setResult(randomOccupancy);
		}
		this.applyOperation();
	}
}