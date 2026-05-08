package ca.concordia.encs.citydata.test.config;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.List;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import ca.concordia.encs.citydata.core.config.RsaKeyProperties;
import ca.concordia.encs.citydata.service.TokenService;

@TestConfiguration
public class TestConfig {

	/**
	 * Generate test RSA keys for JWT operations
	 */
	@Bean
	@Primary
	public RsaKeyProperties rsaKeyProperties() {
		try {
			KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
			keyPairGenerator.initialize(2048);
			KeyPair keyPair = keyPairGenerator.generateKeyPair();

			RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
			RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();

			return new RsaKeyProperties(publicKey, privateKey);
		} catch (Exception e) {
			throw new RuntimeException("Failed to generate test RSA keys", e);
		}
	}

	/**
	 * Test helper for generating authentication tokens
	 */
	@Bean
	public TestAuthHelper testAuthHelper(TokenService tokenService) {
		return new TestAuthHelper(tokenService);
	}

	/**
	 * Inner class: Token generation helper
	 */
	public static class TestAuthHelper {
		private final TokenService tokenService;

		public TestAuthHelper(TokenService tokenService) {
			this.tokenService = tokenService;
		}

		public String getTestToken() {
			return getTokenForUser("testuser", "read");
		}

		public String getTokenForUser(String username, String... authorities) {
			List<SimpleGrantedAuthority> auths = List.of(authorities).stream().map(SimpleGrantedAuthority::new)
					.toList();

			Authentication auth = new UsernamePasswordAuthenticationToken(username, null, auths);

			return tokenService.generateToken(auth);
		}
	}
}