package ca.concordia.encs.citydata.test.producers;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;

import com.google.gson.JsonObject;

import ca.concordia.encs.citydata.core.implementations.JSONProducer;
import ca.concordia.encs.citydata.operations.MergeOperation;
import ca.concordia.encs.citydata.operations.StandardFilteringOperation;
import ca.concordia.encs.citydata.operations.TemporalAggregationOperation;
import ca.concordia.encs.citydata.producers.BuildingProducer;
import ca.concordia.encs.citydata.producers.EnergyConsumptionProducer;
import ca.concordia.encs.citydata.producers.EnvironmentalSensorProducer;
import ca.concordia.encs.citydata.producers.GeometryProducer;
import ca.concordia.encs.citydata.producers.OccupancyProducer;
import ca.concordia.encs.citydata.producers.RoomOccupancyProducer;

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
		final EnergyConsumptionProducer producer1 = new EnergyConsumptionProducer("montreal", null);
		producer1.setCity("montreal");
		producer1.setStartDatetime("2021-09-01 00:00:00");
		producer1.setEndDatetime("2021-09-01 23:59:00");
		producer1.setClientId(1);
		producer1.validateParams();
		producer1.buildQuery();
		producer1.fetch();
		System.out.println(producer1.getResult());
	}

	@Test
	public void testOccupancyProducer() {
		final OccupancyProducer producer = new OccupancyProducer("./src/test/resources/occupancy.csv", null);
		producer.setListSize(2);
		producer.fetch();
		ArrayList<String> result = producer.getResult();
		System.out.println(result);
	}

	@Test
	public void testEnvironmentalSensorProducer() {
		final EnvironmentalSensorProducer producer = new EnvironmentalSensorProducer(
				"./src/test/resources/temperature.csv", null);
		final StandardFilteringOperation operation = new StandardFilteringOperation();
		operation.setSensorId("12504");
		operation.setRoom("221");
		operation.setDate("2025-07-01");

		producer.setOperation(operation);
		producer.fetch();

		ArrayList<String> result = producer.getResult();
		System.out.println(result);
	}

	@Test
	public void testRoomOccupancyProducer() {
		final RoomOccupancyProducer producer = new RoomOccupancyProducer("./src/test/resources/occupancy.csv", null);
		final TemporalAggregationOperation operation = new TemporalAggregationOperation();
		operation.setRoom("411");
		operation.setDate("2025-07-10");
		operation.setStartTime("15:30:00");
		operation.setEndTime("16:00:00");

		producer.setOperation(operation);
		producer.fetch();
		ArrayList<String> result = producer.getResult();
		System.out.println(result);
	}

	@Test
	// GeometryProducer wraps JSONProducer and applies MergeOperation
	public void testGeometryProducer() {
		final GeometryProducer producer = new GeometryProducer();
		producer.setCity("montreal");
		final MergeOperation mergeOperation = new MergeOperation();
		//        producer.setOperation(mergeOperation);
		//        producer.fetch();
		//        System.out.println(producer.getResult());
		//        System.out.println(result);
	}

	@Test
	public void testGeoJsonProducer() {
		final JSONProducer producer = new JSONProducer("./src/test/resources/test_one_building.geojson", null);
		producer.fetch();
		ArrayList<JsonObject> result = producer.getResult();
		System.out.println(result);
	}

}
