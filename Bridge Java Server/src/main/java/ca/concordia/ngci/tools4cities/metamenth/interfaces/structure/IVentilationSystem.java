package ca.concordia.ngci.tools4cities.metamenth.interfaces.structure;

import java.util.List;
import java.util.Map;

import ca.concordia.ngci.tools4cities.metamenth.interfaces.datatypes.IAbstractVentilationComponent;
import ca.concordia.ngci.tools4cities.metamenth.interfaces.datatypes.IDuct;
import ca.concordia.ngci.tools4cities.metamenth.interfaces.datatypes.IVentilationType;

public interface IVentilationSystem {

	String getUID();

	IVentilationType getVentilationType();

	void setVentilationType(IVentilationType ventilationType);

	IDuct getPrincipalDuct();

	void setPrincipalDuct(IDuct principalDuct);

	List<IAbstractVentilationComponent> getComponents(Map<String, Object> searchTerms);

	IVentilationSystem addComponent(IAbstractVentilationComponent component);

	void removeComponent(IAbstractVentilationComponent component);

	String toString();

}
