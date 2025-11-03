package ca.concordia.encs.citydata.producers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
	private String validJsonInput;
	private String inValidJsonInput;
	private String emptyJsonInput;

	@BeforeEach
	public void setUp() {
		this.objectMapper = new ObjectMapper();

		this.validJsonInput = """
								{
				  "building": {
				    "yearBuilt": 1996,
				    "type": "NON_COMMERCIAL",
				    "height": {
				      "unit": "METERS",
				      "value": 15.0
				    },
				    "floorArea": {
				      "unit": "SQUARE_METERS",
				      "value": 50591.3
				    },
				    "address": {
				      "city": "Montreal",
				      "street": "1400 de Maisonneuve Blvd. W.",
				      "province": "QC",
				      "postalCode": "H3G 1M8",
				      "country": "Canada",
				      "coordinates": {
				        "x": 45.497,
				        "y": -73.578
				      }
				    },
				    "floors": [
				      {
				        "number": "1",
				        "type": "REGULAR",
				        "description": "First floor of the building",
				        "size": { "unit": "SQUARE_METERS", "value": 150.0 },
				        "rooms": [
				          {
				            "name": "Room 001",
				            "type": "Office",
				            "size": { "unit": "SQUARE_METERS", "value": 20.0 },
				            "sensors": [
				              {
				                "id": "TMP 01",
				                "measure": "Temperature",
				                "unit": "°C",
				                "type": "THERMO_COUPLE_TYPE_A",
				                "frequency": 900,
				                "data": [10.0, 11.0, 12.0, 13.0, 18.0]
				              }
				            ]
				          }
				        ]
				      }
				    ],
				    "meters": [
				      {
				        "id": "ELECTRICITY_01",
				        "type": "ELECTRICITY",
				        "unit": "KILOWATTS_PER_HOUR",
				        "mode": "AUTOMATIC",
				        "frequency": 45.0
				      }
				    ],
				    "weatherStation": {
				      "id": "LB WS",
				      "data": [
				        { "measure": "RELATIVE_HUMIDITY", "value": 40.0 },
				        { "measure": "RELATIVE_HUMIDITY", "value": 41.0 },
				        { "measure": "RELATIVE_HUMIDITY", "value": 42.0 },
				        { "measure": "RELATIVE_HUMIDITY", "value": 43.0 },
				        { "measure": "RELATIVE_HUMIDITY", "value": 44.0 }
				      ]
				    },
				    "controlSystems": [
				      { "name": "HVAC System", "type": "HVAC" }
				    ]
				  }
				}
								""";

		this.inValidJsonInput = """
								{
				  "building": {
				    "yearBuilt": 1996,
				    "type": "NON_COMMERCIAL",
				    "height": {
				      "unit": "METERS",
				      "value": 15.0
				    },
				    "floorArea": {
				      "unit": "SQUARE_METERS",
				      "value": 50591.3
				    },
				    "address": {
				      "city": "Montreal",
				      "street": "1400 de Maisonneuve Blvd. W.",
				      "province": "QC",
				      "postalCode": "H3G 1M8",
				      "country": "Canada",
				      "coordinates": {
				        "x": 45.497,
				        "y": -73.578

				    },
				    "floors": [
				      {
				        "number": "1",
				        "type": "REGULAR",
				        "description": "First floor of the building",
				        "size": { "unit": "SQUARE_METERS", "value": 150.0 },
				        "rooms": [
				          {
				            "name": "Room 001",
				            "type": "Office",
				            "size": { "unit": "SQUARE_METERS", "value": 20.0 },
				            "sensors": [
				              {
				                "id": "TMP 01",
				                "measure": "Temperature",
				                "unit": "°C",
				                "type": "THERMO_COUPLE_TYPE_A",
				                "frequency": 900,
				                "data": [10.0, 11.0, 12.0, 13.0, 18.0]
				              }
				            ]
				          }
				        ]
				      }
				    ],
				    "meters": [
				      {
				        "id": "ELECTRICITY_01",
				        "type": "ELECTRICITY",
				        "unit": "KILOWATTS_PER_HOUR",
				        "mode": "AUTOMATIC",
				        "frequency": 45.0
				      }
				    ],
				    "weatherStation": {
				      "id": "LB WS",
				      "data": [
				        { "measure": "RELATIVE_HUMIDITY", "value": 40.0 },
				        { "measure": "RELATIVE_HUMIDITY", "value": 41.0 },
				        { "measure": "RELATIVE_HUMIDITY", "value": 42.0 },
				        { "measure": "RELATIVE_HUMIDITY", "value": 43.0 },
				        { "measure": "RELATIVE_HUMIDITY", "value": 44.0 }
				      ]
				    },
				    "controlSystems": [
				      { "name": "HVAC System", "type": "HVAC" }
				    ]
				  }
				}
								""";

		this.emptyJsonInput = """
								{

				}
								""";
	}

	@Test
	public void testCreateBuildingRoundTripConsistency() throws Exception {
		// Parse input JSON
		final JsonNode inputNode = this.objectMapper.readTree(validJsonInput);

		// Perform POST request
		final MvcResult result = mockMvc
				.perform(post("/api/building/create").contentType(MediaType.APPLICATION_JSON).content(validJsonInput))
				.andExpect(status().isOk()).andReturn();

		// Parse response JSON
		final String responseJson = result.getResponse().getContentAsString();
		final JsonNode outputNode = objectMapper.readTree(responseJson);

		// TEST
		assertEquals(inputNode, outputNode);
	}

	@Test
	public void testCreateBuildingRoundTripConsistencyInvalidJson() throws Exception {
		mockMvc.perform(post("/api/building/create").contentType(MediaType.APPLICATION_JSON).content(inValidJsonInput))
				.andExpect(status().isBadRequest());
	}

	@Test
	public void testCreateBuildingRoundTripConsistencyEmptyJson() throws Exception {
		// Perform POST request with empty JSON input
		mockMvc.perform(post("/api/building/create").contentType(MediaType.APPLICATION_JSON).content(emptyJsonInput))
				.andExpect(status().isBadRequest());
	}

	/*@Test
	public void testCreateBuilding_RoundTripConsistencyNormalisedJason() throws Exception {
		// Perform POST request
		final MvcResult result = mockMvc
				.perform(post("/api/building/create").contentType(MediaType.APPLICATION_JSON).content(validJsonInput))
				.andExpect(status().is2xxSuccessful()).andReturn();
	
		// Parse response JSON
		final String outputJson = result.getResponse().getContentAsString();
	
		// TEST
		final PythonEntryServer pes = PythonEntryServer.INSTANCE;
	
		pes.createBuildingFromJson(validJsonInput);
		final IBuilding inputBuilding = pes.getBuilding();
	
		pes.createBuildingFromJson(outputJson);
		final IBuilding outputBuilding = pes.getBuilding();
	
		assertEquals(inputBuilding, outputBuilding);
	}*/
}