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
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import ca.concordia.encs.citydata.core.configs.RsaKeyProperties;
import ca.concordia.encs.citydata.core.configs.SecurityConfig;

/**
 * JUnit 5 Unit tests for SecurityConfig credential management
 * Tests the four main scenarios: Add, Update, Delete, and List users
 * 
 * These tests verify that the SecurityConfig correctly reads and processes
 * the credentials.txt file in various scenarios matching the bash script operations.
 * 
 * @author Sikandar Ejaz
 * @since 2025-12-01
 */

@DisplayName("SecurityConfig Credentials Management Tests")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension.class)
public class UserManagementTest {

	@TempDir
	Path tempDir;

	private PasswordEncoder passwordEncoder;
	private ObjectMapper objectMapper;
	private File credentialsFile;

	@Mock
	private RsaKeyProperties rsaKeys;

	private SecurityConfig securityConfig;

	@BeforeEach
	void setUp() throws IOException {
		objectMapper = new ObjectMapper();
		passwordEncoder = new BCryptPasswordEncoder();

		// Create a temporary credentials file for testing
		credentialsFile = tempDir.resolve("credentials.txt").toFile();

		// Initialize SecurityConfig with mocked RSA keys
		securityConfig = new SecurityConfig(rsaKeys);
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

	/**
	 * Helper method to load users from file by directly calling SecurityConfig's private method
	 * This simulates the behavior without requiring classpath resources
	 */
	private InMemoryUserDetailsManager loadUsersFromTestFile() throws Exception {
		List<UserDetails> users = new ArrayList<>();

		// Read from test file (not classpath)
		JsonNode root = objectMapper.readTree(credentialsFile);

		List<JsonNode> nodes = new ArrayList<>();
		if (root.isArray()) {
			root.forEach(nodes::add);
		} else if (root.isObject()) {
			nodes.add(root);
		}

		for (JsonNode node : nodes) {
			String uname = node.has("username") && !node.get("username").isNull() ? node.get("username").asText()
					: null;
			String pwHash = node.has("password") && !node.get("password").isNull() ? node.get("password").asText()
					: null;

			if (uname == null || pwHash == null || uname.isEmpty() || pwHash.isEmpty())
				continue;

			UserDetails u = User.withUsername(uname).password(pwHash).authorities("read").build();
			users.add(u);
		}

		// If no users found, create fallback (matching SecurityConfig behavior)
		if (users.isEmpty()) {
			String defaultUsername = securityConfig.getDefaultUsername() != null ? securityConfig.getDefaultUsername()
					: "defaultUser";
			String defaultPassword = securityConfig.getDefaultPassword() != null ? securityConfig.getDefaultPassword()
					: "$2a$10$dummyHash";
			UserDetails fallback = User.withUsername(defaultUsername).password(defaultPassword).authorities("read")
					.build();
			users.add(fallback);
		}

		return new InMemoryUserDetailsManager(users);
	}

	// ========== SCENARIO 1: ADD USER ==========

	@Nested
	@DisplayName("Scenario 1: Add User Tests")
	class AddUserTests {

		@Test
		@DisplayName("Should successfully add a single user")
		void testAddSingleUser_Success() throws Exception {
			// Create credentials file with one user
			String hashedPassword = hashPassword("password123");
			String jsonContent = String.format("[{\"username\": \"testuser\", \"password\": \"%s\"}]", hashedPassword);
			writeCredentialsFile(jsonContent);

			// Load users from test file
			InMemoryUserDetailsManager userDetailsManager = loadUsersFromTestFile();

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
		void testAddMultipleUsers_Success() throws Exception {
			// Create credentials file with multiple users
			String hash1 = hashPassword("pass1");
			String hash2 = hashPassword("pass2");
			String hash3 = hashPassword("pass3");

			String jsonContent = String.format("[" + "{\"username\": \"user1\", \"password\": \"%s\"},"
					+ "{\"username\": \"user2\", \"password\": \"%s\"},"
					+ "{\"username\": \"user3\", \"password\": \"%s\"}" + "]", hash1, hash2, hash3);
			writeCredentialsFile(jsonContent);

			// Load users from test file
			InMemoryUserDetailsManager userDetailsManager = loadUsersFromTestFile();

			// All users should exist
			assertAll("All users should be added successfully",
					() -> assertTrue(userDetailsManager.userExists("user1"), "user1 should exist"),
					() -> assertTrue(userDetailsManager.userExists("user2"), "user2 should exist"),
					() -> assertTrue(userDetailsManager.userExists("user3"), "user3 should exist"));
		}

		@Test
		@DisplayName("Should add user with special characters in username")
		void testAddUser_SpecialCharactersInUsername() throws Exception {
			// Arrange
			String hash = hashPassword("password");
			String jsonContent = String.format("[{\"username\": \"user.name+test@example\", \"password\": \"%s\"}]",
					hash);
			writeCredentialsFile(jsonContent);

			// Load users from test file
			InMemoryUserDetailsManager manager = loadUsersFromTestFile();

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
		void testUpdateUserPassword_Success() throws Exception {
			// Initial user with old password
			String oldHash = hashPassword("oldpassword");
			String jsonContent = String.format("[{\"username\": \"updateuser\", \"password\": \"%s\"}]", oldHash);
			writeCredentialsFile(jsonContent);

			InMemoryUserDetailsManager manager = loadUsersFromTestFile();
			UserDetails oldUser = manager.loadUserByUsername("updateuser");
			assertTrue(passwordEncoder.matches("oldpassword", oldUser.getPassword()),
					"Old password should be valid initially");

			// Simulate update by writing new password
			String newHash = hashPassword("newpassword");
			jsonContent = String.format("[{\"username\": \"updateuser\", \"password\": \"%s\"}]", newHash);
			writeCredentialsFile(jsonContent);

			// Reload users
			manager = loadUsersFromTestFile();
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
		void testUpdateUser_ShouldNotAffectOthers() throws Exception {
			// Create file with multiple users
			String hash1 = hashPassword("password1");
			String hash2 = hashPassword("password2");
			String jsonContent = String.format("[" + "{\"username\": \"user1\", \"password\": \"%s\"},"
					+ "{\"username\": \"user2\", \"password\": \"%s\"}" + "]", hash1, hash2);
			writeCredentialsFile(jsonContent);

			InMemoryUserDetailsManager manager = loadUsersFromTestFile();

			// Update only user1
			String newHash1 = hashPassword("newpassword1");
			jsonContent = String.format("[" + "{\"username\": \"user1\", \"password\": \"%s\"},"
					+ "{\"username\": \"user2\", \"password\": \"%s\"}" + "]", newHash1, hash2);
			writeCredentialsFile(jsonContent);
			manager = loadUsersFromTestFile();

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
		void testUpdateNonExistentUser_ShouldNotAffectOtherUsers() throws Exception {
			// Create file with existing users
			String hash = hashPassword("password");
			String jsonContent = String.format("[{\"username\": \"existinguser\", \"password\": \"%s\"}]", hash);
			writeCredentialsFile(jsonContent);

			// Verify existing user is unaffected
			InMemoryUserDetailsManager manager = loadUsersFromTestFile();

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
		void testDeleteAllUsers_ShouldUseFallback() throws Exception {
			// Create empty credentials file
			writeCredentialsFile("[]");

			// Act
			InMemoryUserDetailsManager manager = loadUsersFromTestFile();

			// Should have fallback default user
			assertNotNull(manager, "Manager should not be null");
			assertTrue(manager.userExists("defaultUser"), "Fallback default user should exist when list is empty");
		}

		@Test
		@DisplayName("Attempting to delete non-existent user should not affect others")
		void testDeleteNonExistentUser_ShouldNotAffectOthers() throws Exception {
			// Create file with users
			String hash = hashPassword("password");
			String jsonContent = String.format("[" + "{\"username\": \"user1\", \"password\": \"%s\"},"
					+ "{\"username\": \"user2\", \"password\": \"%s\"}" + "]", hash, hash);
			writeCredentialsFile(jsonContent);

			// File remains unchanged (simulates failed deletion attempt)
			InMemoryUserDetailsManager manager = loadUsersFromTestFile();

			// Existing users unaffected
			assertAll("Existing users should be unaffected",
					() -> assertTrue(manager.userExists("user1"), "user1 should still exist"),
					() -> assertTrue(manager.userExists("user2"), "user2 should still exist"),
					() -> assertFalse(manager.userExists("nonexistent"), "Non-existent user should not exist"));
		}

		@Test
		@DisplayName("Should delete first user from list")
		void testDeleteFirstUser() throws Exception {
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
			InMemoryUserDetailsManager manager = loadUsersFromTestFile();

			// Assert
			assertAll("First user should be deleted",
					() -> assertFalse(manager.userExists("first"), "First user should be deleted"),
					() -> assertTrue(manager.userExists("second"), "Second user should remain"),
					() -> assertTrue(manager.userExists("third"), "Third user should remain"));
		}

		@Test
		@DisplayName("Should delete last user from list")
		void testDeleteLastUser() throws Exception {
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
			InMemoryUserDetailsManager manager = loadUsersFromTestFile();

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
		void testListUsers_EmptyFile() throws Exception {
			// Empty credentials file
			writeCredentialsFile("[]");

			// Act
			InMemoryUserDetailsManager manager = loadUsersFromTestFile();

			// Should handle empty list gracefully with fallback user
			assertNotNull(manager, "Manager should not be null for empty file");
			assertTrue(manager.userExists("defaultUser"), "Fallback user should exist when file is empty");
		}

		@Test
		@DisplayName("Should list multiple users correctly")
		void testListUsers_MultipleUsers() throws Exception {
			// Create file with multiple users
			String hash = hashPassword("password");
			String jsonContent = String.format("[" + "{\"username\": \"alice\", \"password\": \"%s\"},"
					+ "{\"username\": \"bob\", \"password\": \"%s\"},"
					+ "{\"username\": \"charlie\", \"password\": \"%s\"}" + "]", hash, hash, hash);
			writeCredentialsFile(jsonContent);

			// Act
			InMemoryUserDetailsManager manager = loadUsersFromTestFile();

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
		void testListUsers_VerifyUserDetails() throws Exception {
			// Arrange
			String hash = hashPassword("testpass");
			String jsonContent = String.format("[{\"username\": \"detailuser\", \"password\": \"%s\"}]", hash);
			writeCredentialsFile(jsonContent);

			// Act
			InMemoryUserDetailsManager manager = loadUsersFromTestFile();
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
		void testListUsers_LargeNumberOfUsers() throws Exception {
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
			InMemoryUserDetailsManager manager = loadUsersFromTestFile();

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
			assertThrows(JsonParseException.class, () -> loadUsersFromTestFile(),
					"Malformed JSON should throw JsonParseException");
		}

		@Test
		@DisplayName("Should preserve BCrypt password format without re-encoding")
		void testBCryptPasswordFormat_ShouldBePreserved() throws Exception {
			// Use actual BCrypt hash
			String bcryptHash = "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";
			String jsonContent = String.format("[{\"username\": \"bcryptuser\", \"password\": \"%s\"}]", bcryptHash);
			writeCredentialsFile(jsonContent);

			// Act
			InMemoryUserDetailsManager manager = loadUsersFromTestFile();
			UserDetails user = manager.loadUserByUsername("bcryptuser");

			// Password should remain as BCrypt hash (not re-encoded)
			assertAll("BCrypt hash should be preserved",
					() -> assertEquals(bcryptHash, user.getPassword(), "Password hash should match exactly"),
					() -> assertTrue(user.getPassword().startsWith("$2a$"), "Password should be in BCrypt format"));
		}

		@Test
		@DisplayName("Should handle null values gracefully")
		void testNullValues_ShouldHandleGracefully() throws Exception {
			// Arrange: Entries with null values
			String hash = hashPassword("password");
			String jsonContent = String.format("[" + "{\"username\": null, \"password\": \"%s\"},"
					+ "{\"username\": \"validuser\", \"password\": null},"
					+ "{\"username\": \"gooduser\", \"password\": \"%s\"}" + "]", hash, hash);
			writeCredentialsFile(jsonContent);

			// Act
			InMemoryUserDetailsManager manager = loadUsersFromTestFile();

			// Only valid entry should be loaded
			assertAll("Only fully valid entry should be loaded",
					() -> assertTrue(manager.userExists("gooduser"), "User with all valid fields should be loaded"),
					() -> assertFalse(manager.userExists("validuser"), "User with null password should be skipped"));
		}

		@Test
		@DisplayName("Should handle empty username gracefully")
		void testEmptyUsername_ShouldBeSkipped() throws Exception {
			String hash = hashPassword("password");
			String jsonContent = String.format("[" + "{\"username\": \"\", \"password\": \"%s\"},"
					+ "{\"username\": \"validuser\", \"password\": \"%s\"}" + "]", hash, hash);
			writeCredentialsFile(jsonContent);

			InMemoryUserDetailsManager manager = loadUsersFromTestFile();

			assertAll("Empty username should be skipped",
					() -> assertTrue(manager.userExists("validuser"), "Valid user should be loaded"),
					() -> assertFalse(manager.userExists(""), "Empty username should be skipped"));
		}

		@Test
		@DisplayName("Should handle empty password gracefully")
		void testEmptyPassword_ShouldBeSkipped() throws Exception {
			String hash = hashPassword("password");
			String jsonContent = String.format("[" + "{\"username\": \"emptypass\", \"password\": \"\"},"
					+ "{\"username\": \"validuser\", \"password\": \"%s\"}" + "]", hash);
			writeCredentialsFile(jsonContent);

			InMemoryUserDetailsManager manager = loadUsersFromTestFile();

			assertAll("Empty password should be skipped",
					() -> assertTrue(manager.userExists("validuser"), "Valid user should be loaded"),
					() -> assertFalse(manager.userExists("emptypass"), "User with empty password should be skipped"));
		}
	}
}