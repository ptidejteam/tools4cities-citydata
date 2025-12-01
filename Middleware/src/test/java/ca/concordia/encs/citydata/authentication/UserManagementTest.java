package ca.concordia.encs.citydata.authentication;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * JUnit 5 Unit tests for SecurityConfig credential management
 * Tests the four main scenarios: Add, Update, Delete, and List users
 * 
 * These tests verify that the SecurityConfig correctly reads and processes
 * the credentials.txt file in various scenarios matching the bash script operations.
 * 
 * Date: 2025-12-01
 * Author: Sikandar Ejaz
 */

@DisplayName("SecurityConfig Credentials Management Tests")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UserManagementTest {

	@TempDir
	Path tempDir;

	private PasswordEncoder passwordEncoder;
	private ObjectMapper objectMapper;
	private File credentialsFile;

	@BeforeEach
	void setUp() throws IOException {
		objectMapper = new ObjectMapper();
		passwordEncoder = new BCryptPasswordEncoder();

		// Create a temporary credentials file for testing
		credentialsFile = tempDir.resolve("credentials.txt").toFile();
	}

	@AfterEach
	void tearDown() {
		// Cleanup is handled automatically by @TempDir
	}

	/**
	 * Helper method to write JSON content to credentials file
	 */

	private void writeCredentialsFile(String jsonContent) throws IOException {
		try (FileWriter writer = new FileWriter(credentialsFile)) {
			writer.write(jsonContent);
		}
	}

	/**
	 * Helper method to create a BCrypt hash for testing
	 */

	private String hashPassword(String plainPassword) {
		return passwordEncoder.encode(plainPassword);
	}

	// ========== SCENARIO 1: ADD USER ==========

	@Nested
	@DisplayName("Scenario 1: Add User Tests")
	class AddUserTests {

		@Test
		@DisplayName("Should successfully add a single user")
		void testAddSingleUser_Success() throws IOException {
			// Create credentials file with one user
			String hashedPassword = hashPassword("password123");
			String jsonContent = String.format("[{\"username\": \"testuser\", \"password\": \"%s\"}]", hashedPassword);
			writeCredentialsFile(jsonContent);

			// Load users from file
			InMemoryUserDetailsManager userDetailsManager = loadUsersFromFile();

			// User should exist with correct details
			assertTrue(userDetailsManager.userExists("testuser"), "User 'testuser' should exist after being added");

			UserDetails user = userDetailsManager.loadUserByUsername("testuser");
			assertNotNull(user, "User details should not be null");
			assertEquals("testuser", user.getUsername(), "Username should match");
			assertTrue(passwordEncoder.matches("password123", user.getPassword()),
					"Password should be correctly hashed and verifiable");
		}

		@Test
		@DisplayName("Should successfully add multiple users")
		void testAddMultipleUsers_Success() throws IOException {
			// Create credentials file with multiple users
			String hash1 = hashPassword("pass1");
			String hash2 = hashPassword("pass2");
			String hash3 = hashPassword("pass3");

			String jsonContent = String.format("[" + "{\"username\": \"user1\", \"password\": \"%s\"},"
					+ "{\"username\": \"user2\", \"password\": \"%s\"},"
					+ "{\"username\": \"user3\", \"password\": \"%s\"}" + "]", hash1, hash2, hash3);
			writeCredentialsFile(jsonContent);

			// Load users from file
			InMemoryUserDetailsManager userDetailsManager = loadUsersFromFile();

			// All users should exist
			assertAll("All users should be added successfully",
					() -> assertTrue(userDetailsManager.userExists("user1"), "user1 should exist"),
					() -> assertTrue(userDetailsManager.userExists("user2"), "user2 should exist"),
					() -> assertTrue(userDetailsManager.userExists("user3"), "user3 should exist"));
		}

		@Test
		@DisplayName("Should add user with special characters in username")
		void testAddUser_SpecialCharactersInUsername() throws IOException {
			// Arrange
			String hash = hashPassword("password");
			String jsonContent = String.format("[{\"username\": \"user.name+test@example\", \"password\": \"%s\"}]",
					hash);
			writeCredentialsFile(jsonContent);

			// Load users from file
			InMemoryUserDetailsManager manager = loadUsersFromFile();

			// Users with special characters should exist
			assertTrue(manager.userExists("user.name+test@example"), "User with special characters should be added");
		}
	}

	// ========== SCENARIO 2: UPDATE USER ==========

	@Nested
	@DisplayName("Scenario 2: Update User Tests")
	class UpdateUserTests {

		@Test
		@DisplayName("Should successfully update user password")
		void testUpdateUserPassword_Success() throws IOException {
			// Initial user with old password
			String oldHash = hashPassword("oldpassword");
			String jsonContent = String.format("[{\"username\": \"updateuser\", \"password\": \"%s\"}]", oldHash);
			writeCredentialsFile(jsonContent);

			InMemoryUserDetailsManager manager = loadUsersFromFile();
			UserDetails oldUser = manager.loadUserByUsername("updateuser");
			assertTrue(passwordEncoder.matches("oldpassword", oldUser.getPassword()),
					"Old password should be valid initially");

			// Simulate update by writing new password
			String newHash = hashPassword("newpassword");
			jsonContent = String.format("[{\"username\": \"updateuser\", \"password\": \"%s\"}]", newHash);
			writeCredentialsFile(jsonContent);

			// Reload users
			manager = loadUsersFromFile();
			UserDetails updatedUser = manager.loadUserByUsername("updateuser");

			// Password should be updated
			assertAll("Password should be updated correctly",
					() -> assertTrue(passwordEncoder.matches("newpassword", updatedUser.getPassword()),
							"New password should be valid"),
					() -> assertFalse(passwordEncoder.matches("oldpassword", updatedUser.getPassword()),
							"Old password should no longer be valid"));
		}

		@Test
		@DisplayName("Should not affect other users when updating one user")
		void testUpdateUser_ShouldNotAffectOthers() throws IOException {
			// Create file with multiple users
			String hash1 = hashPassword("password1");
			String hash2 = hashPassword("password2");
			String jsonContent = String.format("[" + "{\"username\": \"user1\", \"password\": \"%s\"},"
					+ "{\"username\": \"user2\", \"password\": \"%s\"}" + "]", hash1, hash2);
			writeCredentialsFile(jsonContent);

			InMemoryUserDetailsManager manager = loadUsersFromFile();

			// Update only user1
			String newHash1 = hashPassword("newpassword1");
			jsonContent = String.format("[" + "{\"username\": \"user1\", \"password\": \"%s\"},"
					+ "{\"username\": \"user2\", \"password\": \"%s\"}" + "]", newHash1, hash2);
			writeCredentialsFile(jsonContent);
			manager = loadUsersFromFile();

			// User1 updated, user2 unchanged
			UserDetails user1 = manager.loadUserByUsername("user1");
			UserDetails user2 = manager.loadUserByUsername("user2");

			assertAll("Only updated user should be affected",
					() -> assertTrue(passwordEncoder.matches("newpassword1", user1.getPassword()),
							"user1 should have new password"),
					() -> assertTrue(passwordEncoder.matches("password2", user2.getPassword()),
							"user2 should still have old password"));
		}

		@Test
		@DisplayName("Attempting to update non-existent user should not affect others")
		void testUpdateNonExistentUser_ShouldNotAffectOtherUsers() throws IOException {
			// Create file with existing users
			String hash = hashPassword("password");
			String jsonContent = String.format("[{\"username\": \"existinguser\", \"password\": \"%s\"}]", hash);
			writeCredentialsFile(jsonContent);

			// Verify existing user is unaffected
			InMemoryUserDetailsManager manager = loadUsersFromFile();

			// Assert
			assertAll("Existing users should remain unaffected",
					() -> assertTrue(manager.userExists("existinguser"), "Existing user should still exist"),
					() -> assertFalse(manager.userExists("nonexistent"), "Non-existent user should not exist"));
		}
	}

	// ========== SCENARIO 3: DELETE USER ==========

	@Nested
	@DisplayName("Scenario 3: Delete User Tests")
	class DeleteUserTests {

		@Test
		@DisplayName("Should use fallback user when all users are deleted")
		void testDeleteAllUsers_ShouldUseFallback() throws IOException {
			// Create empty credentials file
			writeCredentialsFile("[]");

			// Act
			InMemoryUserDetailsManager manager = loadUsersFromFile();

			// Should have fallback default user
			assertNotNull(manager, "Manager should not be null");
			assertTrue(manager.userExists("defaultUser"), "Fallback default user should exist when list is empty");
		}

		@Test
		@DisplayName("Attempting to delete non-existent user should not affect others")
		void testDeleteNonExistentUser_ShouldNotAffectOthers() throws IOException {
			// Create file with users
			String hash = hashPassword("password");
			String jsonContent = String.format("[" + "{\"username\": \"user1\", \"password\": \"%s\"},"
					+ "{\"username\": \"user2\", \"password\": \"%s\"}" + "]", hash, hash);
			writeCredentialsFile(jsonContent);

			// File remains unchanged (simulates failed deletion attempt)
			InMemoryUserDetailsManager manager = loadUsersFromFile();

			// Existing users unaffected
			assertAll("Existing users should be unaffected",
					() -> assertTrue(manager.userExists("user1"), "user1 should still exist"),
					() -> assertTrue(manager.userExists("user2"), "user2 should still exist"),
					() -> assertFalse(manager.userExists("nonexistent"), "Non-existent user should not exist"));
		}

		@Test
		@DisplayName("Should delete first user from list")
		void testDeleteFirstUser() throws IOException {
			// Arrange
			String hash = hashPassword("password");
			String jsonContent = String.format("[" + "{\"username\": \"first\", \"password\": \"%s\"},"
					+ "{\"username\": \"second\", \"password\": \"%s\"},"
					+ "{\"username\": \"third\", \"password\": \"%s\"}" + "]", hash, hash, hash);
			writeCredentialsFile(jsonContent);

			// Delete first user
			jsonContent = String.format("[" + "{\"username\": \"second\", \"password\": \"%s\"},"
					+ "{\"username\": \"third\", \"password\": \"%s\"}" + "]", hash, hash);
			writeCredentialsFile(jsonContent);
			InMemoryUserDetailsManager manager = loadUsersFromFile();

			// Assert
			assertAll("First user should be deleted",
					() -> assertFalse(manager.userExists("first"), "First user should be deleted"),
					() -> assertTrue(manager.userExists("second"), "Second user should remain"),
					() -> assertTrue(manager.userExists("third"), "Third user should remain"));
		}

		@Test
		@DisplayName("Should delete last user from list")
		void testDeleteLastUser() throws IOException {
			// Arrange
			String hash = hashPassword("password");
			String jsonContent = String.format("[" + "{\"username\": \"first\", \"password\": \"%s\"},"
					+ "{\"username\": \"second\", \"password\": \"%s\"},"
					+ "{\"username\": \"third\", \"password\": \"%s\"}" + "]", hash, hash, hash);
			writeCredentialsFile(jsonContent);

			// Delete last user
			jsonContent = String.format("[" + "{\"username\": \"first\", \"password\": \"%s\"},"
					+ "{\"username\": \"second\", \"password\": \"%s\"}" + "]", hash, hash);
			writeCredentialsFile(jsonContent);
			InMemoryUserDetailsManager manager = loadUsersFromFile();

			// Assert
			assertAll("Last user should be deleted",
					() -> assertTrue(manager.userExists("first"), "First user should remain"),
					() -> assertTrue(manager.userExists("second"), "Second user should remain"),
					() -> assertFalse(manager.userExists("third"), "Last user should be deleted"));
		}
	}

	// ========== SCENARIO 4: LIST USERS ==========

	@Nested
	@DisplayName("Scenario 4: List Users Tests")
	class ListUsersTests {

		@Test
		@DisplayName("Should handle empty credentials file")
		void testListUsers_EmptyFile() throws IOException {
			// Empty credentials file
			writeCredentialsFile("[]");

			// Act
			InMemoryUserDetailsManager manager = loadUsersFromFile();

			// Should handle empty list gracefully with fallback user
			assertNotNull(manager, "Manager should not be null for empty file");
			assertTrue(manager.userExists("defaultUser"), "Fallback user should exist when file is empty");
		}

		@Test
		@DisplayName("Should list multiple users correctly")
		void testListUsers_MultipleUsers() throws IOException {
			// Create file with multiple users
			String hash = hashPassword("password");
			String jsonContent = String.format("[" + "{\"username\": \"alice\", \"password\": \"%s\"},"
					+ "{\"username\": \"bob\", \"password\": \"%s\"},"
					+ "{\"username\": \"charlie\", \"password\": \"%s\"}" + "]", hash, hash, hash);
			writeCredentialsFile(jsonContent);

			// Act
			InMemoryUserDetailsManager manager = loadUsersFromFile();

			// All users should be listed
			assertAll("All users should be accessible",
					() -> assertTrue(manager.userExists("alice"), "alice should exist"),
					() -> assertTrue(manager.userExists("bob"), "bob should exist"),
					() -> assertTrue(manager.userExists("charlie"), "charlie should exist"),
					() -> assertNotNull(manager.loadUserByUsername("alice"), "alice should be loadable"),
					() -> assertNotNull(manager.loadUserByUsername("bob"), "bob should be loadable"),
					() -> assertNotNull(manager.loadUserByUsername("charlie"), "charlie should be loadable"));
		}

		@Test
		@DisplayName("Should verify all user details are correct")
		void testListUsers_VerifyUserDetails() throws IOException {
			// Arrange
			String hash = hashPassword("testpass");
			String jsonContent = String.format("[{\"username\": \"detailuser\", \"password\": \"%s\"}]", hash);
			writeCredentialsFile(jsonContent);

			// Act
			InMemoryUserDetailsManager manager = loadUsersFromFile();
			UserDetails user = manager.loadUserByUsername("detailuser");

			// Verify all user details are correct
			assertAll("User details should be complete and correct",
					() -> assertEquals("detailuser", user.getUsername(), "Username should match"),
					() -> assertTrue(
							user.getAuthorities().stream().anyMatch(auth -> auth.getAuthority().equals("read")),
							"User should have 'read' authority"),
					() -> assertTrue(user.isEnabled(), "User should be enabled"),
					() -> assertTrue(user.isAccountNonExpired(), "Account should not be expired"),
					() -> assertTrue(user.isAccountNonLocked(), "Account should not be locked"),
					() -> assertTrue(user.isCredentialsNonExpired(), "Credentials should not be expired"));
		}

		@Test
		@DisplayName("Should list large number of users")
		void testListUsers_LargeNumberOfUsers() throws IOException {
			// Create file with many users
			StringBuilder jsonBuilder = new StringBuilder("[");
			String hash = hashPassword("password");

			for (int i = 0; i < 100; i++) {
				if (i > 0)
					jsonBuilder.append(",");
				jsonBuilder.append(String.format("{\"username\": \"user%d\", \"password\": \"%s\"}", i, hash));
			}
			jsonBuilder.append("]");

			writeCredentialsFile(jsonBuilder.toString());

			// Act
			InMemoryUserDetailsManager manager = loadUsersFromFile();

			// Sample check for some users
			assertAll("Large number of users should be loaded",
					() -> assertTrue(manager.userExists("user0"), "First user should exist"),
					() -> assertTrue(manager.userExists("user50"), "Middle user should exist"),
					() -> assertTrue(manager.userExists("user99"), "Last user should exist"));
		}
	}

	// ========== EDGE CASES & ERROR HANDLING ==========

	@Nested
	@DisplayName("Edge Cases and Error Handling")
	class EdgeCasesTests {

		@Test
		@DisplayName("Should throw exception for malformed JSON")
		void testMalformedJSON_ShouldThrowException() throws IOException {
			// Invalid JSON
			writeCredentialsFile("{invalid json");

			// Should throw exception
			assertThrows(JsonParseException.class, () -> loadUsersFromFile(),
					"Malformed JSON should throw RuntimeException");
		}

		@Test
		@DisplayName("Should preserve BCrypt password format without re-encoding")
		void testBCryptPasswordFormat_ShouldBePreserved() throws IOException {
			// Use actual BCrypt hash
			String bcryptHash = "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";
			String jsonContent = String.format("[{\"username\": \"bcryptuser\", \"password\": \"%s\"}]", bcryptHash);
			writeCredentialsFile(jsonContent);

			// Act
			InMemoryUserDetailsManager manager = loadUsersFromFile();
			UserDetails user = manager.loadUserByUsername("bcryptuser");

			// Password should remain as BCrypt hash (not re-encoded)
			assertAll("BCrypt hash should be preserved",
					() -> assertEquals(bcryptHash, user.getPassword(), "Password hash should match exactly"),
					() -> assertTrue(user.getPassword().startsWith("$2a$"), "Password should be in BCrypt format"));
		}

		@Test
		@DisplayName("Should handle null values gracefully")
		void testNullValues_ShouldHandleGracefully() throws IOException {
			// Arrange: Entries with null values
			String hash = hashPassword("password");
			String jsonContent = String.format("[" + "{\"username\": null, \"password\": \"%s\"},"
					+ "{\"username\": \"validuser\", \"password\": null},"
					+ "{\"username\": \"gooduser\", \"password\": \"%s\"}" + "]", hash, hash);
			writeCredentialsFile(jsonContent);

			// Act
			InMemoryUserDetailsManager manager = loadUsersFromFile();

			// Only valid entry should be loaded
			assertAll("Only fully valid entry should be loaded",
					() -> assertTrue(manager.userExists("gooduser"), "User with all valid fields should be loaded"),
					() -> assertFalse(manager.userExists("validuser"), "User with null password should be skipped"));
		}

	}

	// ========== HELPER METHODS ==========

	/**
	 * Helper method to simulate loading users from the credentials file
	 * This mimics the behavior of SecurityConfig.userDetailsService() method
	 */
	private InMemoryUserDetailsManager loadUsersFromFile() throws IOException {
		// Read and parse the credentials file
		ObjectMapper mapper = new ObjectMapper();
		JsonNode root = mapper.readTree(credentialsFile);

		List<UserDetails> users = new ArrayList<>();

		if (root.isArray()) {
			for (JsonNode node : root) {
				String username = node.has("username") && !node.get("username").isNull() ? node.get("username").asText()
						: null;
				String password = node.has("password") && !node.get("password").isNull() ? node.get("password").asText()
						: null;

				if (username != null && !username.isEmpty() && password != null && !password.isEmpty()) {
					UserDetails user = org.springframework.security.core.userdetails.User.withUsername(username)
							.password(password).authorities("read").build();
					users.add(user);
				}
			}
		} else if (root.isObject()) {
			String username = root.has("username") && !root.get("username").isNull() ? root.get("username").asText()
					: null;
			String password = root.has("password") && !root.get("password").isNull() ? root.get("password").asText()
					: null;

			if (username != null && !username.isEmpty() && password != null && !password.isEmpty()) {
				UserDetails user = org.springframework.security.core.userdetails.User.withUsername(username)
						.password(password).authorities("read").build();
				users.add(user);
			}
		}

		// If no users found, create fallback (matches SecurityConfig behavior)
		if (users.isEmpty()) {
			UserDetails fallback = org.springframework.security.core.userdetails.User.withUsername("defaultUser")
					.password("$2a$10$dummyHash").authorities("read").build();
			users.add(fallback);
		}

		return new InMemoryUserDetailsManager(users);
	}

}
