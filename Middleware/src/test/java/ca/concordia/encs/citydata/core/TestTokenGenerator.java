package ca.concordia.encs.citydata.core;

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
 * Author: Sikandar Ejaz 
 * Date: 2025-07-18
 * 
 * Last Update: Fetch "username" for tests from GitHub secrets
 * Author: Sikandar Ejaz
 * Date: 2025-10-01
 */

@SpringBootTest
public class TestTokenGenerator {
	@Autowired
	private TokenService tokenService;

	public static final String TEST_USERNAME = loadUsernameFromEnv();

	private static String loadUsernameFromEnv() {
		String username = System.getenv("TEST_USERNAME");
		if (username == null || username.isBlank()) {
			username = "citydata";
		}
		return username;
	}

	public String getToken() {
		final List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
		final Authentication auth = new UsernamePasswordAuthenticationToken(TEST_USERNAME, null, authorities);
		return tokenService.generateToken(auth);
	}
}