package ca.concordia.ngci.tools4cities.metamenth.interfaces.datatypes;

public interface IEngine extends IAbstractVentilationComponent {

	IAbstractMeasure getPowerRating();

	void setPowerRating(IAbstractMeasure powerRating);

	String getManufacturer();

	void setManufacturer(String manufacturer);

}
