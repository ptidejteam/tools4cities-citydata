package ca.concordia.ngci.tools4cities.metamenth.interfaces.datatypes;

public interface IAbstractVentilationComponent {

	String getUID();

	String getComponentType();

	void setComponentType(String type);

	IAbstractMeasure getCapacity();

	void setCapacity(IAbstractMeasure capacity);

	String toString();

}
