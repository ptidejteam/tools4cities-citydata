package ca.concordia.ngci.tools4cities.metamenth.interfaces.datatypes;

public interface IDuct {

	String getUID();

	IAbstractMeasure getLength();

	void setLength(IAbstractMeasure length);

	IAbstractMeasure getDiameter();

	void setDiameter(IAbstractMeasure diameter);

	String getMaterial();

	void setMaterial(String material);

	String toString();

}
