package ca.concordia.encs.citydata.test.core;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;

import ca.concordia.encs.citydata.core.configs.AppConfig;

/**
 * Integration tests for aDatasetController.
 *
 * Uses {@code @WebMvcTest} to load the full Spring MVC layer (including
 * exception handlers) while keeping the service mocked. Spring Security is
 * satisfied via {@code @WithMockUser} — no token logic is exercised here.
 * 
 * @author Sikandar Ejaz
 * @since 2026-06-01
 */

@SpringBootTest(classes = { AppConfig.class })
@AutoConfigureMockMvc
@ComponentScan(basePackages = "ca.concordia.encs.citydata.core")

class DatasetControllerIntegrationTest {

	/*@Autowired
	private MockMvc mockMvc;
	
	@MockBean
	private DatasetAccessService datasetAccessService;
	
	// PUBLIC dataset — /api/datasets/public
	
	@Nested
	@DisplayName("GET /api/datasets/public")
	class PublicDatasetEndpoint {
	
		@Test
		@WithMockUser(username = "alice")
		@DisplayName("alice (authorised) → 200 with dataset content")
		void authorisedUserGetsPublicDataset() throws Exception {
			when(datasetAccessService.getDatasetContent(DatasetType.PUBLIC))
					.thenReturn("message\nYou have access to Public dataset");
	
			mockMvc.perform(get("/api/datasets/public").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
					.andExpect(jsonPath("$.content").value("message\nYou have access to Public dataset"));
		}
	
		@Test
		@WithMockUser(username = "dave")
		@DisplayName("dave (not in public metadata) → 403 Forbidden")
		void unauthorisedUserGetsForbidden() throws Exception {
			when(datasetAccessService.getDatasetContent(DatasetType.PUBLIC))
					.thenThrow(new AccessDeniedException("dave", "PUBLIC"));
	
			mockMvc.perform(get("/api/datasets/public").accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isForbidden()).andExpect(jsonPath("$.error").exists());
		}
	
		@Test
		@WithMockUser(username = "alice")
		@DisplayName("metadata file corruption → 500 Internal Server Error")
		void metadataErrorReturns500() throws Exception {
			when(datasetAccessService.getDatasetContent(DatasetType.PUBLIC))
					.thenThrow(new MetadataException("Metadata file is empty"));
	
			mockMvc.perform(get("/api/datasets/public").accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isInternalServerError())
					.andExpect(jsonPath("$.error").value("Metadata file is empty"));
		}
	}
	
	// PROTECTED dataset — /api/datasets/protected
	
	@Nested
	@DisplayName("GET /api/datasets/protected")
	class ProtectedDatasetEndpoint {
	
		@Test
		@WithMockUser(username = "alice")
		@DisplayName("alice (authorised) → 200 with dataset content")
		void authorisedUserGetsProtectedDataset() throws Exception {
			when(datasetAccessService.getDatasetContent(DatasetType.PROTECTED))
					.thenReturn("message\nYou have access to Protected dataset");
	
			mockMvc.perform(get("/api/datasets/protected").accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.content").value("message\nYou have access to Protected dataset"));
		}
	
		@Test
		@WithMockUser(username = "dave")
		@DisplayName("dave (authorised for protected) → 200 with dataset content")
		void daveGetsProtectedDataset() throws Exception {
			when(datasetAccessService.getDatasetContent(DatasetType.PROTECTED))
					.thenReturn("message\nYou have access to Protected dataset");
	
			mockMvc.perform(get("/api/datasets/protected").accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.content").value("message\nYou have access to Protected dataset"));
		}
	
		@Test
		@WithMockUser(username = "bob")
		@DisplayName("bob (not in protected metadata) → 403 Forbidden")
		void bobCannotAccessProtectedDataset() throws Exception {
			when(datasetAccessService.getDatasetContent(DatasetType.PROTECTED))
					.thenThrow(new AccessDeniedException("bob", "PROTECTED"));
	
			mockMvc.perform(get("/api/datasets/protected").accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isForbidden()).andExpect(jsonPath("$.error").exists());
		}
	
		@Test
		@WithMockUser(username = "charlie")
		@DisplayName("charlie (not in protected metadata) → 403 Forbidden")
		void charlieCannotAccessProtectedDataset() throws Exception {
			when(datasetAccessService.getDatasetContent(DatasetType.PROTECTED))
					.thenThrow(new AccessDeniedException("charlie", "PROTECTED"));
	
			mockMvc.perform(get("/api/datasets/protected").accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isForbidden()).andExpect(jsonPath("$.error").exists());
		}
	}
	
	// PRIVATE dataset — /api/datasets/private
	
	@Nested
	@DisplayName("GET /api/datasets/private")
	class PrivateDatasetEndpoint {
	
		@Test
		@WithMockUser(username = "alice")
		@DisplayName("alice (only authorised user) → 200 with dataset content")
		void aliceGetsPrivateDataset() throws Exception {
			when(datasetAccessService.getDatasetContent(DatasetType.PRIVATE))
					.thenReturn("message\nYou have access to Private dataset");
	
			mockMvc.perform(get("/api/datasets/private").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
					.andExpect(jsonPath("$.content").value("message\nYou have access to Private dataset"));
		}
	
		@Test
		@WithMockUser(username = "bob")
		@DisplayName("bob cannot access PRIVATE dataset → 403 Forbidden")
		void bobCannotAccessPrivateDataset() throws Exception {
			when(datasetAccessService.getDatasetContent(DatasetType.PRIVATE))
					.thenThrow(new AccessDeniedException("bob", "PRIVATE"));
	
			mockMvc.perform(get("/api/datasets/private").accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isForbidden()).andExpect(jsonPath("$.error").exists());
		}
	
		@Test
		@WithMockUser(username = "dave")
		@DisplayName("dave cannot access PRIVATE dataset → 403 Forbidden")
		void daveCannotAccessPrivateDataset() throws Exception {
			when(datasetAccessService.getDatasetContent(DatasetType.PRIVATE))
					.thenThrow(new AccessDeniedException("dave", "PRIVATE"));
	
			mockMvc.perform(get("/api/datasets/private").accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isForbidden()).andExpect(jsonPath("$.error").exists());
		}
	
		@Test
		@WithMockUser(username = "eve")
		@DisplayName("eve cannot access PRIVATE dataset → 403 Forbidden")
		void eveCannotAccessPrivateDataset() throws Exception {
			when(datasetAccessService.getDatasetContent(DatasetType.PRIVATE))
					.thenThrow(new AccessDeniedException("eve", "PRIVATE"));
	
			mockMvc.perform(get("/api/datasets/private").accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isForbidden()).andExpect(jsonPath("$.error").exists());
		}
	}*/
}
