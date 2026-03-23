package ca.concordia.encs.citydata.core;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;

import com.google.gson.JsonObject;

import ca.concordia.encs.citydata.producers.BuildingProducer;

public class SanityProducerTest {

	@Test
	public void testBuildingProducer() {
		final BuildingProducer producer = new BuildingProducer();
		producer.setBuildingName("mock");
		producer.fetch();
		ArrayList<JsonObject> result = producer.getResult();
		System.out.println(result);
	}
}
