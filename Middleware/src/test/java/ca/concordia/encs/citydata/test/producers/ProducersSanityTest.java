package ca.concordia.encs.citydata.test.producers;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import ca.concordia.encs.citydata.producers.BuildingProducer;
import ca.concordia.encs.citydata.producers.EnergyConsumptionProducer;
import ca.concordia.encs.citydata.producers.OccupancyProducer;
import ca.concordia.encs.citydata.producers.RandomNumberProducer;
import ca.concordia.encs.citydata.producers.RandomStringProducer;

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
	public void testEnergyConsumptionProducer() {
		final EnergyConsumptionProducer producer1 = new EnergyConsumptionProducer();
		producer1.setCity("montreal");
		producer1.setStartDatetime("2021-09-01 00:00:00");
		producer1.setEndDatetime("2021-09-01 23:59:00");
		producer1.setClientId(1);
		producer1.validateParams();
		producer1.buildQuery();
		producer1.fetch();
		ArrayList<JsonArray> result = producer1.getResult();
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

	@Test
	public void testRandomStringProducer() {
		final RandomStringProducer producer = new RandomStringProducer();
		producer.setStringLength(10);
		producer.fetch();
		ArrayList<String> result = producer.getResult();
		System.out.println(result);
	}
}
