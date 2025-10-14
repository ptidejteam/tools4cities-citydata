package ca.concordia.encs.citydata.dto;

import java.util.List;

public class LBBuildingDTO {
	private int yearBuilt;
	private String type;
	private Double heightValue;
	private String heightUnit;
	private Double areaValue;
	private String areaUnit;
	private String city;
	private String street;
	private String province;
	private String postalCode;
	private String country;
	private Double x;
	private Double y;
	private List<String> meters;
	private List<String> controlSystems;
	private List<String> weatherStations;

	// Getters and Setters
	public int getYearBuilt() {
		return yearBuilt;
	}

	public void setYearBuilt(int yearBuilt) {
		this.yearBuilt = yearBuilt;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Double getHeightValue() {
		return heightValue;
	}

	public void setHeightValue(Double heightValue) {
		this.heightValue = heightValue;
	}

	public String getHeightUnit() {
		return heightUnit;
	}

	public void setHeightUnit(String heightUnit) {
		this.heightUnit = heightUnit;
	}

	public Double getAreaValue() {
		return areaValue;
	}

	public void setAreaValue(Double areaValue) {
		this.areaValue = areaValue;
	}

	public String getAreaUnit() {
		return areaUnit;
	}

	public void setAreaUnit(String areaUnit) {
		this.areaUnit = areaUnit;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getStreet() {
		return street;
	}

	public void setStreet(String street) {
		this.street = street;
	}

	public String getProvince() {
		return province;
	}

	public void setProvince(String province) {
		this.province = province;
	}

	public String getPostalCode() {
		return postalCode;
	}

	public void setPostalCode(String postalCode) {
		this.postalCode = postalCode;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public Double getX() {
		return x;
	}

	public void setX(Double x) {
		this.x = x;
	}

	public Double getY() {
		return y;
	}

	public void setY(Double y) {
		this.y = y;
	}

	public List<String> getMeters() {
		return meters;
	}

	public void setMeters(List<String> meters) {
		this.meters = meters;
	}

	public List<String> getControlSystems() {
		return controlSystems;
	}

	public void setControlSystems(List<String> controlSystems) {
		this.controlSystems = controlSystems;
	}

	public List<String> getWeatherStations() {
		return weatherStations;
	}

	public void setWeatherStations(List<String> weatherStations) {
		this.weatherStations = weatherStations;
	}
}