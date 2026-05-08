package ca.concordia.encs.citydata.test.core;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import ca.concordia.encs.citydata.core.config.AppConfig;
import ca.concordia.encs.citydata.core.utils.ReflectionUtils;
import ca.concordia.encs.citydata.test.AbstractTest;
import ca.concordia.encs.citydata.test.PayloadFactory;

/**
 * Apply routes test
 *
 * @author Gabriel C. Ullmann, Sikandar Ejaz
 * @since 2025-06-18
 */

@SpringBootTest(classes = { AppConfig.class })
@AutoConfigureMockMvc
@ComponentScan(basePackages = "ca.concordia.encs.citydata.core")

public class ApplyTest extends AbstractTest {

	private void performPostRequest(String url, String contentType, String content) throws Exception {
		mockMvc.perform(post(url).contentType(contentType).content(content)).andExpect(status().isOk())
				.andExpect(content().string(containsString("result")));
	}

	// Test to check /apply/async with invalid JSON input -- Need to fix
	@Test
	public void whenInvalidReturnIdWrongInput() throws Exception {
		final String invalidSteps = "invalid-json";

		mockMvc.perform(post("/apply/async").header("Authorization", "Bearer " + getToken())
				.contentType(MediaType.APPLICATION_JSON).content(invalidSteps)).andExpect(status().is4xxClientError())
				.andExpect(content().string(containsString("Your query is not a valid JSON file.")));
	}

	// Test to check /apply/async with invalid and unexpected JSON input type
	@Test
	public void whenInvalidReturnIdWrongMediaType() throws Exception {
		final String invalidSteps = "invalid-json";

		mockMvc.perform(post("/apply/async").header("Authorization", "Bearer " + getToken())
				.contentType(MediaType.APPLICATION_NDJSON_VALUE).content(invalidSteps))
				.andExpect(status().is4xxClientError())
				.andExpect(content().string(containsString("Your query is not a valid JSON file.")));
	}

	// Test for GET /async/{runnerId} with a valid runner ID
	@Test
	public void whenValidRunnerId_thenReturnResultOrNotReadyMessage() throws Exception {
		String runnerId = "d593c930-7fed-4c7b-ac52-fff946b78c32";

		mockMvc.perform(get("/apply/async/" + runnerId).header("Authorization", "Bearer " + getToken())
				.contentType(MediaType.APPLICATION_JSON)).andExpect(status().is4xxClientError())
				.andExpect(content().string(containsString("Sorry, your request result is not ready yet.")));
	}

	// Test for invalid runner ID Need to fix -- I (Minette) fixed it, changed 404 to 400 in the status and updated expected message
	@Test
	public void whenInvalidRunnerId_thenReturnNotReadyMessage() throws Exception {
		String invalidRunnerId = "nonexistent-runner-id";

		mockMvc.perform(get("/apply/async/" + invalidRunnerId).header("Authorization", "Bearer " + getToken()))
				.andExpect(status().is(400))
				.andExpect(content().string(containsString("Invalid runner ID format. Please provide a valid UUID.")));
	}

	// Test for sync with wrong media type access
	@Test
	public void testSyncWrongMediaTypeAccess() throws Exception {
		String jsonPayload = PayloadFactory.getBasicQuery();
		mockMvc.perform(post("/apply/sync").contentType("XXX").content(jsonPayload))
				.andExpect(status().is4xxClientError());
	}

	@Test
	public void testGetRequiredField() {
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("testField", "testValue");
		assertEquals("testValue", ReflectionUtils.getRequiredField(jsonObject, "testField").getAsString());
	}

	@Test
	public void testGetRequiredFieldMissing() {
		JsonObject jsonObject = new JsonObject();

		Exception exception = assertThrows(IllegalArgumentException.class, () -> {
			ReflectionUtils.getRequiredField(jsonObject, "missingField");
		});
		String expectedMessage = "missingField";
		assertTrue(exception.getMessage().toLowerCase().contains(expectedMessage.toLowerCase()),
				"Expected message to contain '" + expectedMessage + "' but was: " + exception.getMessage());
	}

	@Test
	public void testInstantiateClass() throws Exception {
		Object instance = ReflectionUtils.instantiateOperation("java.lang.String");
		assertTrue(instance instanceof String);
	}

	@Test
	public void testSetParameters() throws Exception {
		JsonObject param1 = new JsonObject();
		param1.addProperty("name", "length");
		param1.addProperty("value", 5);
		JsonArray params = new JsonArray();
		params.add(param1);
		StringBuilder instance = new StringBuilder();
		ReflectionUtils.setParameters(instance, params);
		assertEquals(5, instance.length());
	}

	@Test
	public void testFindSetterMethod() throws Exception {
		Method method = ReflectionUtils.findSetterMethod(StringBuilder.class, "length");
		assertNotNull(method);
		assertEquals("setLength", method.getName());
	}

	@Test
	public void testRoutesList() throws Exception {

		mockMvc.perform(get("/routes/list").header("Authorization", "Bearer " + getToken())).andExpect(status().isOk())
				.andExpect(content().string(containsString("Method: [")));
	}

	@Test
	public void testOperationsList() throws Exception {

		mockMvc.perform(get("/operations/list").header("Authorization", "Bearer " + getToken()))
				.andExpect(status().isOk()).andExpect(content().string(containsString("ca.concordia.encs.citydata")));
	}

	@Test
	public void testProducersList() throws Exception {

		mockMvc.perform(get("/producers/list").header("Authorization", "Bearer " + getToken()))
				.andExpect(status().isOk()).andExpect(content().string(containsString("ca.concordia.encs.citydata")));
	}
}