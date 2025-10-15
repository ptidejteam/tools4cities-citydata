package ca.concordia.ngci.tools4cities.metamenth.interfaces.datatypes;

// TODO Rename as IRangeMeasure
public interface IAbstractRangeMeasure extends IAbstractMeasure {
	void setMinimum(float minimum);

	float getMinimum();

	void setMaximum(float maximum);

	float getMaximum();
}
