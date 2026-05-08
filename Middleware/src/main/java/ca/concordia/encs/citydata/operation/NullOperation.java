package ca.concordia.encs.citydata.operation;

import java.util.ArrayList;

import ca.concordia.encs.citydata.core.contract.IOperation;
import ca.concordia.encs.citydata.core.implementation.AbstractOperation;

/**
 * Placeholder operation
 * TODO: use it to avoid NullPointerException in Producers
 *
 * @author Gabriel C. Ullmann
 * @since 2025-06-18
 */

public class NullOperation extends AbstractOperation<Object> implements IOperation<Object> {

	@Override
	public ArrayList<Object> apply(ArrayList<Object> inputs) {
		return inputs != null ? inputs : new ArrayList<>();
	}
}
