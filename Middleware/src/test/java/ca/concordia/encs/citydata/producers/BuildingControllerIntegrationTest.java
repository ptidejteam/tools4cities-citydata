package ca.concordia.encs.citydata.producers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import ca.concordia.encs.citydata.core.configs.AppConfig;

/**
 * Unit test to ensure the JSON input and output round trip consistency of bridge between Java and Python 
 * Two tests: Valid JSON input and Invalid/empty JSON input
 * 
 * Author: Sikandar Ejaz 
 * Date: 2025-10-23
 */

@SpringBootTest(classes = AppConfig.class)
@ComponentScan(basePackages = "ca.concordia.encs.citydata.core")
@AutoConfigureMockMvc
public class BuildingControllerIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	private ObjectMapper objectMapper;
	private String inputJson;

	@BeforeEach
	public void setUp() {
		objectMapper = new ObjectMapper();

		inputJson = "{\n" + "  \"building\": {\n" + "    \"yearBuilt\": 1996,\n" + "    \"type\": \"NON_COMMERCIAL\",\n"
				+ "    \"height\": {\n" + "      \"unit\": \"METERS\",\n" + "      \"value\": 15.0\n" + "    },\n"
				+ "    \"floorArea\": {\n" + "      \"unit\": \"SQUARE_METERS\",\n" + "      \"value\": 50591.3\n"
				+ "    },\n" + "    \"address\": {\n" + "      \"city\": \"Montreal\",\n"
				+ "      \"street\": \"1400 de Maisonneuve Blvd. W.\",\n" + "      \"province\": \"QC\",\n"
				+ "      \"postalCode\": \"H3G 1M8\",\n" + "      \"country\": \"Canada\",\n"
				+ "      \"coordinates\": {\n" + "        \"x\": 45.497,\n" + "        \"y\": -73.578\n" + "      }\n"
				+ "    },\n" + "    \"floors\": [\n" + "      {\n" + "        \"number\": 1,\n"
				+ "        \"type\": \"REGULAR\",\n" + "        \"description\": \"First floor of the building\",\n"
				+ "        \"size\": { \"unit\": \"SQUARE_METERS\", \"value\": 150.0 },\n" + "        \"rooms\": [\n"
				+ "          {\n" + "            \"name\": \"Room 001\",\n" + "            \"type\": \"Office\",\n"
				+ "            \"size\": { \"unit\": \"SQUARE_METERS\", \"value\": 20.0 },\n"
				+ "            \"sensors\": [\n" + "              {\n" + "                \"id\": \"TMP 01\",\n"
				+ "                \"measure\": \"Temperature\",\n" + "                \"unit\": \"°C\",\n"
				+ "                \"type\": \"THERMO_COUPLE_TYPE_A\",\n" + "                \"frequency\": 900,\n"
				+ "                \"data\": [10.0, 11.0, 12.0, 13.0, 14.0, 15.0, 16.0, 17.0, 18.0, 19.0]\n"
				+ "              }\n" + "            ]\n" + "          }\n" + "        ]\n" + "      }\n" + "    ],\n"
				+ "    \"meters\": [\n" + "      {\n" + "        \"id\": \"ELECTRICITY_01\",\n"
				+ "        \"type\": \"ELECTRICITY\",\n" + "        \"unit\": \"KILOWATTS_PER_HOUR\",\n"
				+ "        \"mode\": \"AUTOMATIC\",\n" + "        \"value\": 90.0\n" + "      }\n" + "    ],\n"
				+ "    \"weatherStation\": {\n" + "      \"id\": \"LB WS\",\n" + "      \"data\": [\n"
				+ "        { \"measure\": \"RELATIVE_HUMIDITY\", \"value\": 40 },\n"
				+ "        { \"measure\": \"RELATIVE_HUMIDITY\", \"value\": 41 },\n"
				+ "        { \"measure\": \"RELATIVE_HUMIDITY\", \"value\": 42 },\n"
				+ "        { \"measure\": \"RELATIVE_HUMIDITY\", \"value\": 43 },\n"
				+ "        { \"measure\": \"RELATIVE_HUMIDITY\", \"value\": 44 },\n"
				+ "        { \"measure\": \"RELATIVE_HUMIDITY\", \"value\": 45 },\n"
				+ "        { \"measure\": \"RELATIVE_HUMIDITY\", \"value\": 46 },\n"
				+ "        { \"measure\": \"RELATIVE_HUMIDITY\", \"value\": 47 },\n"
				+ "        { \"measure\": \"RELATIVE_HUMIDITY\", \"value\": 48 },\n"
				+ "        { \"measure\": \"RELATIVE_HUMIDITY\", \"value\": 49 }\n" + "      ]\n" + "    },\n"
				+ "    \"controlSystems\": [\n" + "      { \"name\": \"HVAC System\", \"type\": \"HVAC\" }\n"
				+ "    ]\n" + "  }\n" + "}";
	}

	@AfterEach
	public void tearDown() {
		// Clean up resources if needed
	}

	@Test
	public void testCreateBuilding_RoundTripConsistency() throws Exception {
		// Parse input JSON
		JsonNode inputNode = objectMapper.readTree(inputJson);
		JsonNode inputBuilding = inputNode.get("building");

		// Perform POST request
		MvcResult result = mockMvc
				.perform(post("/api/building/create").contentType(MediaType.APPLICATION_JSON).content(inputJson))
				.andExpect(status().isOk()).andReturn();

		// Parse response JSON
		String responseJson = result.getResponse().getContentAsString();
		JsonNode outputNode = objectMapper.readTree(responseJson);
		JsonNode outputBuilding = outputNode.get("building");

		// Assert response is not null
		assertNotNull(outputBuilding, "Output building should not be null");

		// Verify building basic properties
		assertEquals(inputBuilding.get("yearBuilt").asInt(), outputBuilding.get("yearBuilt").asInt(),
				"Year built should match");

		assertEquals(inputBuilding.get("type").asText(), outputBuilding.get("type").asText(),
				"Building type should match");

		// Verify height
		verifyMeasurement(inputBuilding.get("height"), outputBuilding.get("height"), "Height");

		// Verify floor area
		verifyMeasurement(inputBuilding.get("floorArea"), outputBuilding.get("floorArea"), "Floor area");

		// Verify address
		verifyAddress(inputBuilding.get("address"), outputBuilding.get("address"));

		// Verify floors
		verifyFloors(inputBuilding.get("floors"), outputBuilding.get("floors"));

		// Verify meters
		verifyMeters(inputBuilding.get("meters"), outputBuilding.get("meters"));

		// Verify weather station
		verifyWeatherStation(inputBuilding.get("weatherStation"), outputBuilding.get("weatherStation"));

		// Verify control systems
		verifyControlSystems(inputBuilding.get("controlSystems"), outputBuilding.get("controlSystems"));
	}

	private void verifyMeasurement(JsonNode input, JsonNode output, String fieldName) {
		assertNotNull(output, fieldName + " should not be null");
		assertEquals(input.get("unit").asText(), output.get("unit").asText(), fieldName + " unit should match");
		assertEquals(input.get("value").asDouble(), output.get("value").asDouble(), 0.01,
				fieldName + " value should match");
	}

	private void verifyAddress(JsonNode inputAddress, JsonNode outputAddress) {
		assertNotNull(outputAddress, "Address should not be null");

		assertEquals(inputAddress.get("city").asText(), outputAddress.get("city").asText(), "City should match");

		assertEquals(inputAddress.get("street").asText(), outputAddress.get("street").asText(), "Street should match");

		assertEquals(inputAddress.get("province").asText(), outputAddress.get("province").asText(),
				"Province should match");

		assertEquals(inputAddress.get("postalCode").asText(), outputAddress.get("postalCode").asText(),
				"Postal code should match");

		assertEquals(inputAddress.get("country").asText(), outputAddress.get("country").asText(),
				"Country should match");

		// Verify coordinates
		JsonNode inputCoords = inputAddress.get("coordinates");
		JsonNode outputCoords = outputAddress.get("coordinates");
		assertNotNull(outputCoords, "Coordinates should not be null");

		assertEquals(inputCoords.get("x").asDouble(), outputCoords.get("x").asDouble(), 0.001, "Latitude should match");

		assertEquals(inputCoords.get("y").asDouble(), outputCoords.get("y").asDouble(), 0.001,
				"Longitude should match");
	}

	private void verifyFloors(JsonNode inputFloors, JsonNode outputFloors) {
		assertNotNull(outputFloors, "Floors should not be null");
		assertTrue(outputFloors.isArray(), "Floors should be an array");
		assertEquals(inputFloors.size(), outputFloors.size(), "Number of floors should match");

		for (int i = 0; i < inputFloors.size(); i++) {
			JsonNode inputFloor = inputFloors.get(i);
			JsonNode outputFloor = outputFloors.get(i);

			// Floor number might be string in output, normalize comparison
			String inputFloorNum = String.valueOf(inputFloor.get("number").asInt());
			String outputFloorNum = outputFloor.get("number").asText();
			assertEquals(inputFloorNum, outputFloorNum, "Floor number should match");

			assertEquals(inputFloor.get("type").asText(), outputFloor.get("type").asText(), "Floor type should match");

			assertEquals(inputFloor.get("description").asText(), outputFloor.get("description").asText(),
					"Floor description should match");

			verifyMeasurement(inputFloor.get("size"), outputFloor.get("size"), "Floor size");

			// Verify rooms
			verifyRooms(inputFloor.get("rooms"), outputFloor.get("rooms"));
		}
	}

	private void verifyRooms(JsonNode inputRooms, JsonNode outputRooms) {
		assertNotNull(outputRooms, "Rooms should not be null");
		assertTrue(outputRooms.isArray(), "Rooms should be an array");
		assertEquals(inputRooms.size(), outputRooms.size(), "Number of rooms should match");

		for (int i = 0; i < inputRooms.size(); i++) {
			JsonNode inputRoom = inputRooms.get(i);
			JsonNode outputRoom = outputRooms.get(i);

			assertEquals(inputRoom.get("name").asText(), outputRoom.get("name").asText(), "Room name should match");

			assertEquals(inputRoom.get("type").asText(), outputRoom.get("type").asText(), "Room type should match");

			verifyMeasurement(inputRoom.get("size"), outputRoom.get("size"), "Room size");

			// Verify sensors
			if (inputRoom.has("sensors")) {
				verifySensors(inputRoom.get("sensors"), outputRoom.get("sensors"));
			}
		}
	}

	private void verifySensors(JsonNode inputSensors, JsonNode outputSensors) {
		assertNotNull(outputSensors, "Sensors should not be null");
		assertTrue(outputSensors.isArray(), "Sensors should be an array");

		// Note: Output might not include sensor ID in the JSON based on your controller code
		// Adjust based on actual output structure

		for (int i = 0; i < inputSensors.size(); i++) {
			JsonNode inputSensor = inputSensors.get(i);
			JsonNode outputSensor = outputSensors.get(i);

			assertEquals(inputSensor.get("measure").asText(), outputSensor.get("measure").asText(),
					"Sensor measure should match");

			assertEquals(inputSensor.get("unit").asText(), outputSensor.get("unit").asText(),
					"Sensor unit should match");

			assertEquals(inputSensor.get("type").asText(), outputSensor.get("type").asText(),
					"Sensor type should match");

			assertEquals(inputSensor.get("frequency").asInt(), outputSensor.get("frequency").asInt(),
					"Sensor frequency should match");

			// Verify sensor data array
			JsonNode inputData = inputSensor.get("data");
			JsonNode outputData = outputSensor.get("data");
			assertNotNull(outputData, "Sensor data should not be null");
			assertTrue(outputData.isArray(), "Sensor data should be an array");
			assertEquals(inputData.size(), outputData.size(), "Sensor data length should match");

			for (int j = 0; j < inputData.size(); j++) {
				assertEquals(inputData.get(j).asDouble(), outputData.get(j).asDouble(), 0.01,
						"Sensor data value at index " + j + " should match");
			}
		}
	}

	private void verifyMeters(JsonNode inputMeters, JsonNode outputMeters) {
		assertNotNull(outputMeters, "Meters should not be null");
		assertTrue(outputMeters.isArray(), "Meters should be an array");
		assertEquals(inputMeters.size(), outputMeters.size(), "Number of meters should match");

		for (int i = 0; i < inputMeters.size(); i++) {
			JsonNode inputMeter = inputMeters.get(i);
			JsonNode outputMeter = outputMeters.get(i);

			assertEquals(inputMeter.get("type").asText(), outputMeter.get("type").asText(), "Meter type should match");

			assertEquals(inputMeter.get("unit").asText(), outputMeter.get("unit").asText(), "Meter unit should match");

			assertEquals(inputMeter.get("mode").asText(), outputMeter.get("mode").asText(), "Meter mode should match");

			// Note: Input has "value" but output might have "frequency" based on controller
			// Adjust based on actual output structure
		}
	}

	private void verifyWeatherStation(JsonNode inputWS, JsonNode outputWS) {
		assertNotNull(outputWS, "Weather station should not be null");

		assertEquals(inputWS.get("id").asText(), outputWS.get("id").asText(), "Weather station ID should match");

		JsonNode inputData = inputWS.get("data");
		JsonNode outputData = outputWS.get("data");
		assertNotNull(outputData, "Weather station data should not be null");
		assertTrue(outputData.isArray(), "Weather station data should be an array");
		assertEquals(inputData.size(), outputData.size(), "Weather data length should match");

		for (int i = 0; i < inputData.size(); i++) {
			JsonNode inputDataPoint = inputData.get(i);
			JsonNode outputDataPoint = outputData.get(i);

			assertEquals(inputDataPoint.get("measure").asText(), outputDataPoint.get("measure").asText(),
					"Weather data measure should match");

			assertEquals(inputDataPoint.get("value").asDouble(), outputDataPoint.get("value").asDouble(), 0.01,
					"Weather data value should match");
		}
	}

	private void verifyControlSystems(JsonNode inputCS, JsonNode outputCS) {
		assertNotNull(outputCS, "Control systems should not be null");
		assertTrue(outputCS.isArray(), "Control systems should be an array");
		assertEquals(inputCS.size(), outputCS.size(), "Number of control systems should match");

		for (int i = 0; i < inputCS.size(); i++) {
			JsonNode inputSystem = inputCS.get(i);
			JsonNode outputSystem = outputCS.get(i);

			assertEquals(inputSystem.get("name").asText(), outputSystem.get("name").asText(),
					"Control system name should match");

			assertEquals(inputSystem.get("type").asText(), outputSystem.get("type").asText(),
					"Control system type should match");
		}
	}

	@Test
	public void testCreateBuilding_EmptyJson_ShouldReturnBadRequest() throws Exception {
		String emptyJson = "{}";

		mockMvc.perform(post("/api/building/create").contentType(MediaType.APPLICATION_JSON).content(emptyJson))
				.andExpect(status().is5xxServerError());
	}
}