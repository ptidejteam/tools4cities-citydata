package ca.concordia.encs.citydata.test.producers;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;

import com.google.gson.JsonObject;

import ca.concordia.encs.citydata.producers.BuildingProducer;
import ca.concordia.encs.citydata.producers.OccupancyProducer;

public class SanityProducerTest {

	@Test
	public void testBuildingProducer() {
		final BuildingProducer producer = new BuildingProducer();
		producer.setBuildingName("mock");
		producer.fetch();
		ArrayList<JsonObject> result = producer.getResult();
		System.out.println(result);
	}

	@Test
	public void testOccupancyProducer() {
		final OccupancyProducer producer = new OccupancyProducer();
		producer.setListSize(0);
		producer.fetch();
		ArrayList<String> result = producer.getResult();
		System.out.println(result);
	}
}
