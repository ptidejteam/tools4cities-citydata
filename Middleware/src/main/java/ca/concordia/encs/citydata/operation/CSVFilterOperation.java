package ca.concordia.encs.citydata.operation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ca.concordia.encs.citydata.core.contract.IOperation;
import ca.concordia.encs.citydata.core.contract.IRunner;
import ca.concordia.encs.citydata.core.implementation.AbstractOperation;

public class CSVFilterOperation extends AbstractOperation<String> implements IOperation<String> {

	private String addressFilter;
	private String startDateFilter;
	private String endDateFilter;
	private IRunner observer;

	public void setAddressFilter(String addressFilter) {
		this.addressFilter = addressFilter;
	}

	public void setStartDateFilter(String startDateFilter) {
		this.startDateFilter = startDateFilter;
	}
	
	public void setEndDateFilter(String endDateFilter) {
		this.endDateFilter = endDateFilter;
	}

	public ArrayList<String> apply(ArrayList<String> input) {
		ArrayList<String> result = new ArrayList<>();
		if (input == null || input.isEmpty()) 
			return result;

		String[] headers = input.get(0).split(",");
        String[] addresses = addressFilter != null ? addressFilter.split(";") : null;

        for (int i = 1; i < input.size(); i++) {
            String line = input.get(i);
            if (line == null || line.trim().isEmpty()) continue;

            String[] cols = line.split(",");
            String address = cols[0].trim();

            if (addresses != null) {
                boolean matchFound = false;
                for (String filter : addresses) {
                    if (address.contains(filter.trim())) {
                        matchFound = true;
                        break;
                    }
                }
                if (!matchFound) continue;
            }

            for (int j = 1; j < headers.length; j++) {
                String date = headers[j].trim();
                if (startDateFilter != null && date.compareTo(startDateFilter) < 0) continue;
                if (endDateFilter != null && date.compareTo(endDateFilter) > 0) continue;
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