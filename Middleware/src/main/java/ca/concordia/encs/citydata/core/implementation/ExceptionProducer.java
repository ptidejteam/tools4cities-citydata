package ca.concordia.encs.citydata.core.implementation;

import java.util.ArrayList;

import ca.concordia.encs.citydata.core.contract.IProducer;

/**
 * This producer was created for the sole purpose of returning Exceptions when
 * it is not possible to throw them in Runners (e.g. when it is enclosed in a
 * run() method within a Thread)
 *
 * @author Gabriel C. Ullmann
 * @since 2025-01-01
 */

//Need to discuss with Yann, and then probably move this back to producers package

public non-sealed class ExceptionProducer extends AbstractProducer<String> implements IProducer<String> {

	public ExceptionProducer(Exception e) {
		final ArrayList<String> result = new ArrayList<>();
		result.add(e.getMessage());
		result.add("caused by: " + e.getCause());
		this.setResult(result);
	}

	@Override
	public void fetch() {
		System.out.println("The fetch method is unimplemented in the ExceptionProducer and shall not be used!");
	}
}