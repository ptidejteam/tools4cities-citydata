package ca.concordia.ngci.tools4cities.metamenth.interfaces.datatypes;

import ca.concordia.ngci.tools4cities.metamenth.enums.IAbstractEnum;

public interface IVentilationType extends IAbstractEnum {

	String getTypeName();

	enum Type {
		AIR_HANDLING_UNIT("AirHandlingUnit");

		private final String value;

		Type(String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}

	}

}
