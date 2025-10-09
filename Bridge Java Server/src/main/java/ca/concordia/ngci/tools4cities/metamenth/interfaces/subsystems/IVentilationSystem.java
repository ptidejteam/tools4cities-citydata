package ca.concordia.ngci.tools4cities.metamenth.interfaces.subsystems;

import ca.concordia.ngci.tools4cities.metamenth.interfaces.subsystems.hvac_components.IDuct;

public interface IVentilationSystem {

	String getUID();

	String getVentilationType();

	void setVentilationType(String ventilationType);

	IDuct getPrincipalDuct();

	void setPrincipalDuct(IDuct principalDuct);

	String toString();

}
