package ca.concordia.encs.citydata.test.producers;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;

import com.google.gson.JsonObject;

import ca.concordia.encs.citydata.producers.BuildingProducer;
import ca.concordia.encs.citydata.producers.OccupancyProducer;
import ca.concordia.encs.citydata.producers.RandomNumberProducer;

public class ProducersSanityTest {

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
		producer.setListSize(2);
		producer.fetch();
		ArrayList<String> result = producer.getResult();
		System.out.println(result);
	}

	@Test
	public void testRandomNumberProducer() {
		final RandomNumberProducer producer = new RandomNumberProducer();
		producer.setListSize(2);
		producer.fetch();
		ArrayList<Integer> result = producer.getResult();
		System.out.println(result);
	}
}
