package ca.concordia.ngci.tools4cities.metamenth.interfaces.subsystems.hvac_components;

public interface IDuct {

	String getUID();

	String toString();

	String getName();

	void setName(String name);

	String getDuctType();

	void setDuctType(String ductType);

}
