package ca.concordia.encs.citydata.core;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import ca.concordia.encs.citydata.config.TestConfig;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestConfig.class)
@TestPropertySource(locations = "classpath:application-test.properties")
public abstract class BaseIntegrationTest {

	@Autowired
	protected MockMvc mockMvc;

	@Autowired
	protected TestConfig.TestAuthHelper authHelper;

	protected String getToken() {
		return authHelper.getTestToken();
	}
}