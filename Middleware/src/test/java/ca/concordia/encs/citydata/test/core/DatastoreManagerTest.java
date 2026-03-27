package ca.concordia.encs.citydata.test.core;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;

import ca.concordia.encs.citydata.core.configs.AppConfig;
import ca.concordia.encs.citydata.core.contracts.IDatastoreManager;
import ca.concordia.encs.citydata.datastores.DatastoreManager;
import ca.concordia.encs.citydata.test.AbstractTest;
import ca.concordia.encs.citydata.test.config.TestConfig;

/**
* DatastoreManager test to check existing datastores.
* @author Minette Zongo, Rushin Dipak Makwana
* @since 2025-08-19
*/

@SpringBootTest(classes = { AppConfig.class, TestConfig.class })
@AutoConfigureMockMvc
@ComponentScan(basePackages = "ca.concordia.encs.citydata.core")
public class DatastoreManagerTest extends AbstractTest {

	private DatastoreManager datastoreManager;

	@BeforeEach
	void setUp() {
		datastoreManager = DatastoreManager.getInstance();
	}

	@Test
	public void testDatastoresExist() {
		assertTrue(datastoreManager.hasStore(IDatastoreManager.DatastoreType.IN_MEMORY),
				"IN_MEMORY datastore should exist");

		assertTrue(datastoreManager.hasStore(IDatastoreManager.DatastoreType.DISK), "DISK datastore should exist");

		assertTrue(datastoreManager.hasStore(IDatastoreManager.DatastoreType.MONGODB),
				"MONGODB datastore should exist");

		assertNotNull(datastoreManager.getStore(IDatastoreManager.DatastoreType.IN_MEMORY),
				"IN_MEMORY datastore should be initialized");
		assertNotNull(datastoreManager.getStore(IDatastoreManager.DatastoreType.DISK),
				"DISK datastore should be initialized");
		assertNotNull(datastoreManager.getStore(IDatastoreManager.DatastoreType.MONGODB),
				"MONGODB datastore should be initialized");
	}
}
