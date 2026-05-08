package ca.concordia.encs.citydata.test.core;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.context.WebApplicationContext;

import ca.concordia.encs.citydata.core.config.AppConfig;
import ca.concordia.encs.citydata.test.AbstractTest;

/**
 * App health routes test
 *
 * @author Minette Zongo, Sikandar Ejaz
 * @since 2025-06-18
 */

@SpringBootTest(classes = AppConfig.class)
@AutoConfigureMockMvc
@ComponentScan(basePackages = "ca.concordia.encs.citydata.core")
public class AppHealthTest extends AbstractTest {

	@Autowired
	private WebApplicationContext webApplicationContext;

	// Test for ping route
	@Test
	public void testPingRoute() throws Exception {
		mockMvc.perform(get("/health/ping")).andExpect(status().isOk())
				.andExpect(content().string(org.hamcrest.Matchers.startsWith("CITYdata running")));
	}
}
