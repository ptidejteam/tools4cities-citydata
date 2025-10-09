package ca.concordia.ngci.tools4cities.metamenth.interfaces.subsystems;

import java.util.List;

public interface IHvacSystem {

	String getUID();

	List<IVentilationSystem> getVentilationSystems();

	void addVentilationSystem(IVentilationSystem ventilationSystem);

	String toString();

}
