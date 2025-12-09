package ca.concordia.encs.citydata.core.configs;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;

/**
 * JWT Authentication Implementation
 * Author: Sikandar Ejaz, Rushin D. Makwana
 * Date: 2025-07-18
 * 
 * Update: Multi-user authentication added
 * Author: Sikandar Ejaz
 * Last Update: 2025-09-28
 * 
 * Update: 2025-11-03
 * Fixed issue is user credentials management
 * 
 * Last Update: 2025-12-02
 * Removed unused code
 */

@EnableWebSecurity
@Configuration
public class SecurityConfig {

	private final RsaKeyProperties rsaKeys;

	@Value("${security.credentials.path:classpath:scripts/credentials/credentials.txt}")
	private String credentialsPath;

	@Value("${security.default.username:defaultUser}")
	private String defaultUsername;

	@Value("${security.default.password:$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG}")
	private String defaultPassword;

	public SecurityConfig(RsaKeyProperties rsaKeys) {
		this.rsaKeys = rsaKeys;
	}

	private List<JsonNode> readCredentialNodes() throws IOException {
		// If no path configured, return empty (will use fallback)
		if (credentialsPath == null || credentialsPath.isEmpty()) {
			return Collections.emptyList();
		}

		InputStream input;

		if (credentialsPath.startsWith("classpath:")) {
			String resourcePath = credentialsPath.substring("classpath:".length());
			input = getClass().getClassLoader().getResourceAsStream(resourcePath);
			if (input == null) {
				return Collections.emptyList();
			}
		} else {
			File file = new File(credentialsPath);
			if (!file.exists()) {
				return Collections.emptyList();
			}
			input = new FileInputStream(file);
		}

		try (input) {
			ObjectMapper mapper = new ObjectMapper();
			JsonNode root = mapper.readTree(input);

			if (root.isArray()) {
				List<JsonNode> list = new ArrayList<>();
				root.forEach(list::add);
				return list;
			} else if (root.isObject()) {
				return Collections.singletonList(root);
			} else {
				return Collections.emptyList();
			}
		}
	}

	public String getDefaultUsername() {
		return defaultUsername;
	}

	public String getDefaultPassword() {
		return defaultPassword;
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	@ConditionalOnMissingBean // Add this
	public InMemoryUserDetailsManager userDetailsService(PasswordEncoder encoder) {
		List<UserDetails> users = new ArrayList<>();
		try {
			List<JsonNode> nodes = readCredentialNodes();
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
		} catch (IOException e) {
			throw new RuntimeException("Failed to load users from credentials file: " + credentialsPath, e);
		}

		if (users.isEmpty()) {
			UserDetails fallback = User.withUsername(defaultUsername).password(defaultPassword).authorities("read")
					.build();
			users.add(fallback);
		}
		return new InMemoryUserDetailsManager(users);
	}

	@Bean
	public AuthenticationManager authManager(UserDetailsService userDetailsService, PasswordEncoder encoder) {
		DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
		authProvider.setUserDetailsService(userDetailsService);
		authProvider.setPasswordEncoder(encoder);
		return new ProviderManager(authProvider);
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		return http.cors(Customizer.withDefaults()).csrf(AbstractHttpConfigurer::disable)
				.authorizeHttpRequests(
						auth -> auth
								.requestMatchers("/authenticate", "/home", "/health/ping", "/producers/list",
										"/operations/list", "/routes/list", "/error")
								.permitAll().anyRequest().authenticated())
				.oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)).build();
	}

	@Bean
	public JwtDecoder jwtDecoder() {
		return NimbusJwtDecoder.withPublicKey(rsaKeys.publicKey()).build();
	}

	@Bean
	public JwtEncoder jwtEncoder() {
		JWK jwk = new RSAKey.Builder(rsaKeys.publicKey()).privateKey(rsaKeys.privateKey()).build();
		JWKSource<SecurityContext> jwks = new ImmutableJWKSet<>(new JWKSet(jwk));
		return new NimbusJwtEncoder(jwks);
	}
}