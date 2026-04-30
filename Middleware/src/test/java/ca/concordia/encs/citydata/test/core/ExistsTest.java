package ca.concordia.encs.citydata.test.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import ca.concordia.encs.citydata.test.AbstractTest;
import ca.concordia.encs.citydata.test.PayloadFactory;

/**
 * ExistsController routes test
 *
 * @author Minette Zongo, Sikandar Ejaz
 * @since 2025-02-26
*/

public class ExistsTest extends AbstractTest {

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
}