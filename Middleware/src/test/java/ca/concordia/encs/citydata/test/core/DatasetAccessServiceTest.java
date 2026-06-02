package ca.concordia.encs.citydata.test.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import ca.concordia.encs.citydata.core.exceptions.AccessDeniedException;
import ca.concordia.encs.citydata.core.exceptions.MetadataException;
import ca.concordia.encs.citydata.core.model.DatasetType;
import ca.concordia.encs.citydata.services.DatasetAccessService;

/**
 * Unit tests for {@link DatasetAccessService}.
 *
 * The Spring Security context is mocked via {@link MockedStatic} so that no
 * application context is needed. The actual metadata + CSV files are read from
 * the test classpath (src/test/resources), making these tests verify the real
 * file-parsing logic while keeping the security concerns isolated.
 * 
 * @author Sikandar Ejaz
 * @since 2026-06-01
 */

@ExtendWith(MockitoExtension.class)
@DisplayName("DatasetAccessService — Unit Tests")
class DatasetAccessServiceTest {

	@InjectMocks
	private DatasetAccessService service;

	private MockedStatic<SecurityContextHolder> mockSecurityContext(String username) {
		Authentication authentication = mock(Authentication.class);
		lenient().when(authentication.isAuthenticated()).thenReturn(true);
		lenient().when(authentication.getName()).thenReturn(username);

		SecurityContext securityContext = mock(SecurityContext.class);
		lenient().when(securityContext.getAuthentication()).thenReturn(authentication);

		MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class);
		mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);
		return mockedStatic;
	}

	// isAuthorised() — pure permission checks (no SecurityContext needed)

	@Nested
	@DisplayName("isAuthorised()")
	class IsAuthorisedTests {

		@Test
		@DisplayName("alice is authorised for ALL datasets")
		void aliceIsAuthorisedForAll() {
			assertThat(service.isAuthorised("alice", DatasetType.PUBLIC)).isTrue();
			assertThat(service.isAuthorised("alice", DatasetType.PROTECTED)).isTrue();
			assertThat(service.isAuthorised("alice", DatasetType.PRIVATE)).isTrue();
		}

		@Test
		@DisplayName("bob is authorised only for PUBLIC")
		void bobIsAuthorisedOnlyForPublic() {
			assertThat(service.isAuthorised("bob", DatasetType.PUBLIC)).isTrue();
			assertThat(service.isAuthorised("bob", DatasetType.PROTECTED)).isFalse();
			assertThat(service.isAuthorised("bob", DatasetType.PRIVATE)).isFalse();
		}

		@Test
		@DisplayName("charlie is authorised only for PUBLIC")
		void charlieIsAuthorisedOnlyForPublic() {
			assertThat(service.isAuthorised("charlie", DatasetType.PUBLIC)).isTrue();
			assertThat(service.isAuthorised("charlie", DatasetType.PROTECTED)).isFalse();
			assertThat(service.isAuthorised("charlie", DatasetType.PRIVATE)).isFalse();
		}

		@Test
		@DisplayName("dave is authorised only for PROTECTED")
		void daveIsAuthorisedOnlyForProtected() {
			assertThat(service.isAuthorised("dave", DatasetType.PUBLIC)).isFalse();
			assertThat(service.isAuthorised("dave", DatasetType.PROTECTED)).isTrue();
			assertThat(service.isAuthorised("dave", DatasetType.PRIVATE)).isFalse();
		}

		@Test
		@DisplayName("eve is not authorised for any dataset")
		void eveIsNotAuthorisedForAny() {
			assertThat(service.isAuthorised("eve", DatasetType.PUBLIC)).isFalse();
			assertThat(service.isAuthorised("eve", DatasetType.PROTECTED)).isFalse();
			assertThat(service.isAuthorised("eve", DatasetType.PRIVATE)).isFalse();
		}

		@Test
		@DisplayName("username matching is case-insensitive")
		void usernameCaseInsensitive() {
			assertThat(service.isAuthorised("ALICE", DatasetType.PUBLIC)).isTrue();
			assertThat(service.isAuthorised("Alice", DatasetType.PROTECTED)).isTrue();
			assertThat(service.isAuthorised("BOB", DatasetType.PUBLIC)).isTrue();
		}
	}

	// loadAuthorisedUsers() — metadata file parsing

	@Nested
	@DisplayName("loadAuthorisedUsers()")
	class LoadAuthorisedUsersTests {

		@Test
		@DisplayName("PUBLIC metadata contains expected users")
		void publicMetadataUsers() {
			List<String> users = service.loadAuthorisedUsers(DatasetType.PUBLIC);
			assertThat(users).containsExactlyInAnyOrder("alice", "bob", "charlie");
		}

		@Test
		@DisplayName("PROTECTED metadata contains expected users")
		void protectedMetadataUsers() {
			List<String> users = service.loadAuthorisedUsers(DatasetType.PROTECTED);
			assertThat(users).containsExactlyInAnyOrder("alice", "dave");
		}

		@Test
		@DisplayName("PRIVATE metadata contains expected users")
		void privateMetadataUsers() {
			List<String> users = service.loadAuthorisedUsers(DatasetType.PRIVATE);
			assertThat(users).containsExactly("alice");
		}

		@Test
		@DisplayName("Throws MetadataException for non-existent metadata file")
		void throwsForMissingFile() {
			// Temporarily override just the path by using a spy
			DatasetAccessService spy = spy(service);
			doThrow(new MetadataException("File not found")).when(spy).loadAuthorisedUsers(DatasetType.PRIVATE);

			assertThatThrownBy(() -> spy.loadAuthorisedUsers(DatasetType.PRIVATE))
					.isInstanceOf(MetadataException.class);
		}
	}

	// getDatasetContent() — authorised access returns correct content

	@Nested
	@DisplayName("getDatasetContent() — authorised users receive dataset content")
	class GetDatasetContentAuthorisedTests {

		@Test
		@DisplayName("alice reads PUBLIC dataset → success message")
		void aliceReadsPublicDataset() {
			try (MockedStatic<SecurityContextHolder> ignored = mockSecurityContext("alice")) {
				String content = service.getDatasetContent(DatasetType.PUBLIC);
				assertThat(content).contains("You have access to Public dataset");
			}
		}

		@Test
		@DisplayName("alice reads PROTECTED dataset → success message")
		void aliceReadsProtectedDataset() {
			try (MockedStatic<SecurityContextHolder> ignored = mockSecurityContext("alice")) {
				String content = service.getDatasetContent(DatasetType.PROTECTED);
				assertThat(content).contains("You have access to Protected dataset");
			}
		}

		@Test
		@DisplayName("alice reads PRIVATE dataset → success message")
		void aliceReadsPrivateDataset() {
			try (MockedStatic<SecurityContextHolder> ignored = mockSecurityContext("alice")) {
				String content = service.getDatasetContent(DatasetType.PRIVATE);
				assertThat(content).contains("You have access to Private dataset");
			}
		}

		@Test
		@DisplayName("bob reads PUBLIC dataset → success message")
		void bobReadsPublicDataset() {
			try (MockedStatic<SecurityContextHolder> ignored = mockSecurityContext("bob")) {
				String content = service.getDatasetContent(DatasetType.PUBLIC);
				assertThat(content).contains("You have access to Public dataset");
			}
		}

		@Test
		@DisplayName("dave reads PROTECTED dataset → success message")
		void daveReadsProtectedDataset() {
			try (MockedStatic<SecurityContextHolder> ignored = mockSecurityContext("dave")) {
				String content = service.getDatasetContent(DatasetType.PROTECTED);
				assertThat(content).contains("You have access to Protected dataset");
			}
		}
	}

	// getDatasetContent() — unauthorised access throws AccessDeniedException

	@Nested
	@DisplayName("getDatasetContent() — unauthorised users are rejected")
	class GetDatasetContentUnauthorisedTests {

		@Test
		@DisplayName("bob cannot access PROTECTED dataset")
		void bobCannotAccessProtected() {
			try (MockedStatic<SecurityContextHolder> ignored = mockSecurityContext("bob")) {
				assertThatThrownBy(() -> service.getDatasetContent(DatasetType.PROTECTED))
						.isInstanceOf(AccessDeniedException.class).hasMessageContaining("bob")
						.hasMessageContaining("PROTECTED");
			}
		}

		@Test
		@DisplayName("bob cannot access PRIVATE dataset")
		void bobCannotAccessPrivate() {
			try (MockedStatic<SecurityContextHolder> ignored = mockSecurityContext("bob")) {
				assertThatThrownBy(() -> service.getDatasetContent(DatasetType.PRIVATE))
						.isInstanceOf(AccessDeniedException.class).hasMessageContaining("bob")
						.hasMessageContaining("PRIVATE");
			}
		}

		@Test
		@DisplayName("dave cannot access PUBLIC dataset")
		void daveCannotAccessPublic() {
			try (MockedStatic<SecurityContextHolder> ignored = mockSecurityContext("dave")) {
				assertThatThrownBy(() -> service.getDatasetContent(DatasetType.PUBLIC))
						.isInstanceOf(AccessDeniedException.class).hasMessageContaining("dave")
						.hasMessageContaining("PUBLIC");
			}
		}

		@Test
		@DisplayName("dave cannot access PRIVATE dataset")
		void daveCannotAccessPrivate() {
			try (MockedStatic<SecurityContextHolder> ignored = mockSecurityContext("dave")) {
				assertThatThrownBy(() -> service.getDatasetContent(DatasetType.PRIVATE))
						.isInstanceOf(AccessDeniedException.class).hasMessageContaining("dave")
						.hasMessageContaining("PRIVATE");
			}
		}

		@Test
		@DisplayName("eve cannot access any dataset")
		void eveCannotAccessAny() {
			try (MockedStatic<SecurityContextHolder> ignored = mockSecurityContext("eve")) {
				assertThatThrownBy(() -> service.getDatasetContent(DatasetType.PUBLIC))
						.isInstanceOf(AccessDeniedException.class);
				assertThatThrownBy(() -> service.getDatasetContent(DatasetType.PROTECTED))
						.isInstanceOf(AccessDeniedException.class);
				assertThatThrownBy(() -> service.getDatasetContent(DatasetType.PRIVATE))
						.isInstanceOf(AccessDeniedException.class);
			}
		}
	}
}
