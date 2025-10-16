package ca.concordia.ngci.tools4cities.metamenth.interfaces.measureinstruments;

import java.util.HashMap;
import java.util.List;

import ca.concordia.ngci.tools4cities.metamenth.enums.MeterMeasureMode;
import ca.concordia.ngci.tools4cities.metamenth.enums.MeterType;

public interface IMeter {
	void addMeterMeasure(IMeterMeasure measure);

	String getAccumulationFrequency();

	boolean getDataAccumulated();

	String getManufacturer();

	double getMeasurementFrequency();

	String getMeasurementUnit();

	String getMeasureMode();

	String getMeterLocation();

	List<IMeterMeasure> getMeterMeasureByDate(String fromDateStr, String toDateStr);

	List<IMeterMeasure> getMeterMeasures(HashMap<String, Object> searchTerms);

	String getMeterType();

	String getDeviceID();

	String getUID();

	void setDeviceID(String deviceID);

	void setAccumulationFrequency(String accummulationFrequency);

	void setDataAccumulated(boolean dataAccummulated);

	void setManufacturer(String manufacturer);

	void setMeasurementFrequency(double measurementFrequency);

	void setMeasurementUnit(String measurementUnit);

	void setMeasureMode(MeterMeasureMode meterMeasureMode);

	void setMeterLocation(String location);

	void setMeterType(MeterType meterType);

	String toString();
}
