package ca.concordia.ngci.tools4cities.metamenth.interfaces.structure;

import java.util.List;

public interface IHVAC {

	String getUID();

	List<IVentilationSystem> getVentilationSystems();

	void addVentilationSystem(IVentilationSystem ventilationSystem);

	String toString();

}
