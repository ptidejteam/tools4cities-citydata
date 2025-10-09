package ca.concordia.ngci.tools4cities.metamenth.enums;

import java.util.Optional;

public interface IAbstractEnum {

	/**
	 * Returns the corresponding enum type for the given string value.
	 * Implementations should handle fuzzy matching or normalization of the input.
	 *
	 * @param value input string (e.g., "Air Handling Unit" → AIR_HANDLING_UNIT)
	 * @return Optional containing the matching enum constant, or empty if not found
	 * 
	 *         Explanation: The Python version uses fuzzy string matching to find
	 *         the closest enum member. In Java, this would be implemented by
	 *         concrete enums (e.g., VentilationTypeEnum) implementing this
	 *         interface. We use Optional for safe return semantics instead of null.
	 *
	 **/

	Optional<? extends Enum<?>> getEnumType(String value);

}
