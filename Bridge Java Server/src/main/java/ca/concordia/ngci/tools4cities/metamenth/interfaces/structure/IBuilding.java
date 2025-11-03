package ca.concordia.ngci.tools4cities.metamenth.interfaces.structure;

import java.util.List;
import java.util.Map;

import ca.concordia.ngci.tools4cities.metamenth.interfaces.datatypes.IAbstractMeasure;
import ca.concordia.ngci.tools4cities.metamenth.interfaces.datatypes.IAddress;
import ca.concordia.ngci.tools4cities.metamenth.interfaces.datatypes.IZone;
import ca.concordia.ngci.tools4cities.metamenth.interfaces.measureinstruments.IMeter;
import ca.concordia.ngci.tools4cities.metamenth.interfaces.measureinstruments.IWeatherStation;
import ca.concordia.ngci.tools4cities.metamenth.interfaces.subsystems.IBuildingControlSystem;

public interface IBuilding {
	void addControlSystem(IBuildingControlSystem buildingControlSystem);

	void addFloor(IFloor floor);

	void addMeter(IMeter meter);

	IBuilding addWeatherStation(IWeatherStation weatherStation);

	IAddress getAddress();

	List<IBuildingControlSystem> getBuildingControlSystem();

	String getBuildingType();

	int getConstructionYear();

	IEnvelope getEnvelope();

	IAbstractMeasure getFloorArea();

	IFloor getFloorById(String id);

	IFloor getFloorByNumber(Object number);

	List<IFloor> getFloors(Map<String, Object> searchTerms);

	IAbstractMeasure getHeight();

	IAbstractMeasure getInternalMass();

	IMeter getMeterById(String id);

	List<IMeter> getMeterByType(String meterType);

	List<IMeter> getMeters(Map<String, Object> searchTerms);

	String getUID();

	IWeatherStation getWeatherStation(String name);

	IZone getZones();

	Boolean removeWeatherStation(IWeatherStation weatherStation);

	boolean equals(Object other);

	int hashCode();

	void setAddress(IAddress address);

	void setBuildingType(String buildingType);

	void setConstructionYear(int year);

	void setEnvelope(IEnvelope envelope);

	void setFloorArea(IAbstractMeasure floorArea);

	void setHeight(IAbstractMeasure height);

	void setInternalMass(IAbstractMeasure internalMass);

	String toString();

}
