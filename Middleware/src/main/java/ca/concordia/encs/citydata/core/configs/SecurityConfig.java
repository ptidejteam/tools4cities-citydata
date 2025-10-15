package ca.concordia.encs.citydata.core.configs;

import java.io.IOException;
import java.io.InputStream;

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
 */

@EnableWebSecurity
@Configuration
public class SecurityConfig {

	private final RsaKeyProperties rsaKeys;
	private String defaultUsername;
	private String defaultPassword;

	public SecurityConfig(RsaKeyProperties rsaKeys) {
		this.rsaKeys = rsaKeys;
		loadCredentialsFromTxt();
	}

	private void loadCredentialsFromTxt() {
		try (InputStream input = getClass().getClassLoader()
				.getResourceAsStream("scripts/credentials/credentials.txt")) {
			if (input == null) {
				throw new RuntimeException("Unable to find credentials.txt in resources folder");
			}

			ObjectMapper mapper = new ObjectMapper();
			JsonNode node = mapper.readTree(input);

			this.defaultUsername = node.has("username") ? node.get("username").asText() : "defaultUser";
			this.defaultPassword = node.has("password") ? node.get("password").asText() : "$2a$10$dummyHash";

		} catch (IOException e) {
			throw new RuntimeException("Failed to load credentials.txt", e);
		}
	}

	public String getDefaultUsername() {
		return defaultUsername;
	}

	public String getDefaultPassword() {
		return defaultPassword; // this is already a BCrypt hash
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public InMemoryUserDetailsManager userDetailsService(PasswordEncoder encoder) {
		UserDetails user = User.withUsername(defaultUsername).password(defaultPassword) // already BCrypt
				.authorities("read").build();
		return new InMemoryUserDetailsManager(user);
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
				.authorizeHttpRequests(auth -> auth
						.requestMatchers("/authenticate", "/home", "/health/ping", "/producers/list",
								"/operations/list", "/routes/list", "/error", "/api/building/create")
						.permitAll().anyRequest().authenticated())
				.oauth2ResourceServer(oauth2 -> oauth2.jwt())
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
