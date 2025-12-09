package ca.concordia.encs.citydata.helpers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import ca.concordia.encs.citydata.services.TokenService;

/**
 * Helper to generate test tokens
 * This is just a utility - not part of the test infrastructure
 */

@Component
public class TestAuthHelper {

	@Autowired
	private TokenService tokenService;

	public String getTestToken() {
		return getTokenForUser("testuser", "read");
	}

	public String getTokenForUser(String username, String... authorities) {
		List<SimpleGrantedAuthority> auths = List.of(authorities).stream().map(SimpleGrantedAuthority::new).toList();

		Authentication auth = new UsernamePasswordAuthenticationToken(username, null, auths);

		return tokenService.generateToken(auth);
	}
}