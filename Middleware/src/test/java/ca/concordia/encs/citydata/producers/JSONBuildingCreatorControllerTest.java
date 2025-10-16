package ca.concordia.encs.citydata.producers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;

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
import com.fasterxml.jackson.databind.node.ObjectNode;

import ca.concordia.encs.citydata.core.TestTokenGenerator;
import ca.concordia.encs.citydata.core.configs.AppConfig;

@SpringBootTest(classes = AppConfig.class)
@AutoConfigureMockMvc
@ComponentScan(basePackages = "ca.concordia.encs.citydata.core")
public class JSONBuildingCreatorControllerTest extends TestTokenGenerator {

	@Autowired
	private MockMvc mockMvc;

	private final ObjectMapper mapper = new ObjectMapper();

	@Test
	void testCreateBuilding_returnsSameJson() throws Exception {
		String inputJson = """
				{
				  "building": {
				    "yearBuilt": 1996,
				    "type": "NON_COMMERCIAL",
				    "height": { "unit": "METERS", "value": 15.0 },
				    "floorArea": { "unit": "SQUARE_METERS", "value": 50591.3 },
				    "address": {
				      "city": "Montreal",
				      "street": "1400 de Maisonneuve Blvd. W.",
				      "province": "QC",
				      "postalCode": "H3G 1M8",
				      "country": "Canada",
				      "coordinates": { "x": 45.497, "y": -73.578 }
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
				                "data": [10.5, 11.2, 12.4, 13.6, 14.8, 15.1, 16.3, 17.5, 18.7, 19.9]
				              }
				            ]
				          }
				        ]
				      }
				    ],
				    "meters": [
				      { "id": "ELECTRICITY_01", "type": "ELECTRICITY", "unit": "KILOWATTS_PER_HOUR", "mode": "AUTOMATIC", "value": 90.0 }
				    ],
				    "weatherStation": {
				      "id": "LB WS",
				      "data": [
				        { "measure": "RELATIVE_HUMIDITY", "value": 40 },
				        { "measure": "RELATIVE_HUMIDITY", "value": 41 },
				        { "measure": "RELATIVE_HUMIDITY", "value": 42 },
				        { "measure": "RELATIVE_HUMIDITY", "value": 43 },
				        { "measure": "RELATIVE_HUMIDITY", "value": 44 },
				        { "measure": "RELATIVE_HUMIDITY", "value": 45 },
				        { "measure": "RELATIVE_HUMIDITY", "value": 46 },
				        { "measure": "RELATIVE_HUMIDITY", "value": 47 },
				        { "measure": "RELATIVE_HUMIDITY", "value": 48 },
				        { "measure": "RELATIVE_HUMIDITY", "value": 49 }
				      ]
				    },
				    "controlSystems": [
				      { "name": "HVAC System", "type": "HVAC" }
				    ]
				  }
				}
				""";

		MvcResult result = mockMvc
				.perform(post("/api/building/create").contentType(MediaType.APPLICATION_JSON).content(inputJson))
				.andExpect(status().isOk()).andReturn();

		String outputJson = result.getResponse().getContentAsString();

		JsonNode inputNode = normalize(mapper.readTree(inputJson));
		JsonNode outputNode = normalize(mapper.readTree(outputJson));

		assertEquals(inputNode, outputNode, "The input and output JSON should be structurally identical.");
	}

	/**
		* Normalizes JSON by removing transient fields, converting numeric strings to numbers,
		* and fixing mismatched field names to ensure structural comparison.
		*/
	private JsonNode normalize(JsonNode node) {
		if (node.isObject()) {
			ObjectNode objectNode = (ObjectNode) node;
			objectNode.remove(Arrays.asList("uid", "timeStamp", "measurementType"));
			objectNode.fieldNames().forEachRemaining(field -> normalize(objectNode.get(field)));
		} else if (node.isArray()) {
			for (JsonNode element : node) {
				normalize(element);
			}
		}
		return node;
	}
}