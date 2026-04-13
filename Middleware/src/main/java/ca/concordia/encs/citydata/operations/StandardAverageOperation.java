package ca.concordia.encs.citydata.operations;

import java.util.ArrayList;
import java.util.HashMap;

import ca.concordia.encs.citydata.core.contracts.IOperation;
import ca.concordia.encs.citydata.core.contracts.IRunner;
import ca.concordia.encs.citydata.core.implementations.AbstractOperation;

/**
 * This operation filters environmental value (either temperature or relative humidity) readings for selected sensors over a 
 * given date and optional time range, then computes the average temperature/humidity value for each target sensor.
 * @author Minette Zongo M.
 * @date 2025-10-07
 */

public class StandardAverageOperation extends AbstractOperation<String> implements IOperation<String> {

	private String sensorIds;
	private String date;
	private String startTime;
	private String endTime;
	private IRunner observer;

	public void setSensorIds(String sensorIds) {
		this.sensorIds = sensorIds;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}

	@Override
	public ArrayList<String> apply(ArrayList<String> input) {
		String[] targetSensors = sensorIds != null ? sensorIds.split(",") : new String[0];

		HashMap<String, ArrayList<Double>> sensorMap = new HashMap<>();

		for (String line : input) {
			if (line == null || line.trim().isEmpty())
				continue;

			String[] cols = line.split(",");
			if (cols.length < 3)
				continue;

			String timestamp = cols[0].trim();
			String lineSensorId = cols[1].trim();
			String humidityValue = cols[2].trim();

			String lineDate = timestamp.length() >= 10 ? timestamp.substring(0, 10) : "";
			String lineTime = timestamp.length() >= 19 ? timestamp.substring(11, 19) : "";

			boolean matchesSensor = false;
			for (String targetSensor : targetSensors) {
				if (lineSensorId.equals(targetSensor.trim())) {
					matchesSensor = true;
					break;
				}
			}

			boolean matchesDate = (date == null || date.isEmpty() || lineDate.equals(date));

			boolean withinTimeRange = true;
			if (startTime != null && !startTime.isEmpty() && endTime != null && !endTime.isEmpty()) {
				withinTimeRange = lineTime.compareTo(startTime) >= 0 && lineTime.compareTo(endTime) <= 0;
			}

			if (matchesSensor && matchesDate && withinTimeRange) {
				try {
					double humidity = Double.parseDouble(humidityValue);

					if (!sensorMap.containsKey(lineSensorId)) {
						sensorMap.put(lineSensorId, new ArrayList<>());
					}
					sensorMap.get(lineSensorId).add(humidity);

				} catch (NumberFormatException e) {
				}
			}
		}

		ArrayList<String> result = new ArrayList<>();

		for (String targetSensor : targetSensors) {
			String sensor = targetSensor.trim();
			ArrayList<Double> values = sensorMap.get(sensor);

			if (values != null && !values.isEmpty()) {
				double sum = 0.0;
				for (double value : values) {
					sum += value;
				}
				double average = sum / values.size();

				result.add("sensor," + sensor + ",average," + average + ",date," + date + ",count," + values.size());
			} else {
				result.add("sensor," + sensor + ",average,0.0,date," + date + ",count,0");
			}
		}

		return result;
	}

	@Override
	public void addObserver(IRunner aRunner) {
		this.observer = aRunner;
	}

	@Override
	public void notifyObservers() {
		if (observer != null) {
			observer.newOperationApplied(this);
		}
	}
}
