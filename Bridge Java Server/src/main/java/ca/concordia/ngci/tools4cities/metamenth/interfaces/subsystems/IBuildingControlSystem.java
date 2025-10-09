package ca.concordia.ngci.tools4cities.metamenth.interfaces.subsystems;

public interface IBuildingControlSystem {

	String getName();

	void setName(String name);

	void setHvacSystem(IHvacSystem hvacSystem);

	IHvacSystem getHvacSystem();

	String toString();

}
