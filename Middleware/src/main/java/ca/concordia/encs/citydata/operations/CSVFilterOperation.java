package ca.concordia.encs.citydata.operations;

import java.util.ArrayList;

import ca.concordia.encs.citydata.core.contracts.IOperation;
import ca.concordia.encs.citydata.core.contracts.IRunner;
import ca.concordia.encs.citydata.core.implementations.AbstractOperation;

public class CSVFilterOperation extends AbstractOperation<String> implements IOperation<String> {

	private String addressFilter;
	private String dateFilter;
	private IRunner observer;

	public void setAddressFilter(String addressFilter) {
		this.addressFilter = addressFilter;
	}

	public void setDateFilter(String dateFilter) {
		this.dateFilter = dateFilter;
	}

	public ArrayList<String> apply(ArrayList<String> input) {
		ArrayList<String> result = new ArrayList<>();
		if (input == null || input.isEmpty()) return result;

		String[] headers = input.get(0).split(",");

		for (int i = 1; i < input.size(); i++) {
			String line = input.get(i);
			if (line == null || line.trim().isEmpty()) continue;

			String[] cols = line.split(",");
			String address = cols[0].trim();

			if (addressFilter != null && !address.contains(addressFilter)) continue;

			for (int j = 1; j < headers.length; j++) {
				String date = headers[j].trim();
				if (dateFilter != null && !date.equals(dateFilter)) continue;
				result.add(address + "," + date + "," + cols[j].trim());
			}
		}

		return result;
	}

	public void addObserver(IRunner aRunner) { 
		this.observer = aRunner; 
	}

	public void notifyObservers() {
		if (observer != null) observer.newOperationApplied(this);
	}
}
