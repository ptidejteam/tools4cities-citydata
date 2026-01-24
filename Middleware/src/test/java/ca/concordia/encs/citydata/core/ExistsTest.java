package ca.concordia.encs.citydata.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import ca.concordia.encs.citydata.PayloadFactory;

/**
 * ExistsController routes test
 *
 * @author Minette Zongo
 * @since 2025-02-26
 *  
 * Last Update: 2025-07-18 
 * Author Sikandar Ejaz 
 * Fixed failing tests after implementing Authentication
*/

public class ExistsTest extends BaseIntegrationTest {

	@Test
	void testQueryExists() throws Exception {
		String jsonPayload = PayloadFactory.getExampleQuery("stringProducerRandom");

		MvcResult syncResult = mockMvc
				.perform(post("/apply/sync").header("Authorization", "Bearer " + getToken())
						.contentType(MediaType.APPLICATION_JSON).content(jsonPayload))
				.andExpect(status().isOk()).andReturn();

		MvcResult existsResult = mockMvc
				.perform(post("/exists/").header("Authorization", "Bearer " + getToken())
						.contentType(MediaType.APPLICATION_JSON).content(jsonPayload))
				.andExpect(status().isOk()).andReturn();

		String responseContent = existsResult.getResponse().getContentAsString();
		assertNotEquals("[]", responseContent);
	}

	@Test
	void testQueryNotExists() throws Exception {
		String jsonPayload = PayloadFactory.getExampleQuery("ckanMetadataProducerListDatasets");

		MvcResult existsResult = mockMvc
				.perform(post("/exists/").header("Authorization", "Bearer " + getToken())
						.contentType(MediaType.APPLICATION_JSON).content(jsonPayload))
				.andExpect(status().isNotFound()).andReturn();

		String responseContent = existsResult.getResponse().getContentAsString();
		assertEquals("[]", responseContent);
	}

	@Test
	void testBrokenJsonQuery() throws Exception {
		String jsonPayload = PayloadFactory.getInvalidJson();

		mockMvc.perform(post("/exists/").header("Authorization", "Bearer " + getToken())
				.contentType(MediaType.APPLICATION_JSON).content(jsonPayload))
				.andExpect(status().isInternalServerError());
	}

	@Test
	void testQueryExistsFollowedBySync() throws Exception {
		String jsonPayload = PayloadFactory.getExampleQuery("stringProducerRandom");

		MvcResult existsResult = mockMvc.perform(post("/exists/").header("Authorization", "Bearer " + getToken())
				.contentType(MediaType.APPLICATION_JSON).content(jsonPayload)).andReturn();

		String responseContent = existsResult.getResponse().getContentAsString();
		int status = existsResult.getResponse().getStatus();

		if (status == 404 || responseContent.equals("[]")) {
			MvcResult syncResult = mockMvc.perform(post("/apply/sync").header("Authorization", "Bearer " + getToken())
					.contentType(MediaType.APPLICATION_JSON).content(jsonPayload)).andReturn();

			int syncStatus = syncResult.getResponse().getStatus();
			String syncResponse = syncResult.getResponse().getContentAsString();

			System.out.println("apply/sync Status: " + syncStatus);
			System.out.println("apply/sync Response: " + syncResponse);

			if (syncStatus != 200) {
				fail("apply/sync failed with status: " + syncStatus + " and response: " + syncResponse);
			}
		} else if (status == 200) {
			assertNotEquals("[]", responseContent);
		} else {
			fail("Unexpected status code: " + status + ". Response content: " + responseContent);
		}
	}
}