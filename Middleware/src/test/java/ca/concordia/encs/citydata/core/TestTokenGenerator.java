package ca.concordia.encs.citydata.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import ca.concordia.encs.citydata.services.TokenService;

/**
 * Added global token generation for running tests
 * @author Sikandar Ejaz 
 * @since 2025-07-18
 */

@SpringBootTest
public class TestTokenGenerator {
	@Autowired
	private TokenService tokenService;

	public static final String TEST_USERNAME = loadUsernameFromFile();

	private static String loadUsernameFromFile() {
		try (InputStream in = TestTokenGenerator.class.getClassLoader()
				.getResourceAsStream("scripts/credentials/credentials.txt")) {

			if (in == null) {
				throw new IllegalStateException("credentials.txt not found in resources");
			}
			String content = new String(in.readAllBytes()).trim();

			int start = content.indexOf("\"username\"") + 11;
			start = content.indexOf("\"", start) + 1;
			int end = content.indexOf("\"", start);
			return content.substring(start, end);

		} catch (IOException e) {
			throw new UncheckedIOException("Failed to load username from file", e);
		}
	}

	public String getToken() {
		final List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
		final Authentication auth = new UsernamePasswordAuthenticationToken(TEST_USERNAME, null, authorities);
		return tokenService.generateToken(auth);

		//
		// POST /authenticate route with username/password obtained from the environment
		// Cf. https://stackoverflow.com/questions/71949369/how-to-use-github-secrets-or-github-environment-variables-in-build-gradle-file
		// Cf. https://docs.github.com/en/actions/how-tos/write-workflows/choose-what-workflows-do/use-secrets
	}
}