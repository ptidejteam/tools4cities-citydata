package ca.concordia.encs.citydata.core.controllers;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import ca.concordia.encs.citydata.core.configs.SecurityConfig;
import ca.concordia.encs.citydata.core.utils.LoginRequest;
import ca.concordia.encs.citydata.services.TokenService;
import jakarta.servlet.http.HttpServletRequest;

/**
 * JWT Multi-user authentication implementation
 * @author Sikandar Ejaz 
 * @since 18-07-2025
 */

@RestController
public class AuthController {

	private final TokenService tokenService;
	private final AuthenticationManager authenticationManager;
	private final SecurityConfig securityConfig;
	private final String defaultCredentialsWarning = "WARNING: you are using default credentials to authenticate! "
			+ "Your CITYdata instance is NOT PROTECTED! \n"
			+ "Please register your own list of trusted credentials by running: "
			+ "./src/main/resources/scripts/credentials-manager.sh. \n"
			+ "Once you register your credentials, the default credentials will be disabled to prevent unauthorized access.";

	public AuthController(TokenService tokenService, AuthenticationManager authenticationManager,
			SecurityConfig securityConfig) {
		this.tokenService = tokenService;
		this.authenticationManager = authenticationManager;
		this.securityConfig = securityConfig;
	}

	@PostMapping("/authenticate")
	public ResponseEntity<String> token(@RequestBody(required = false) LoginRequest userLogin) {
		try {
			Authentication authentication = authenticationManager
					.authenticate(new UsernamePasswordAuthenticationToken(userLogin.username(), userLogin.password()));

			if (userLogin.username().equalsIgnoreCase(this.securityConfig.getDefaultUsername())) {
				System.out.println(defaultCredentialsWarning);
				return ResponseEntity.ok(tokenService.generateToken(authentication) + "\n" + defaultCredentialsWarning);
			}
			return ResponseEntity.ok(tokenService.generateToken(authentication));

		} catch (final AuthenticationException e) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
		}
	}

	@GetMapping("/authenticate")
	public ResponseEntity<String> tokenBasicAuth(HttpServletRequest request) {
		try {
			String authHeader = request.getHeader("Authorization");

			if (authHeader == null || !authHeader.startsWith("Basic ")) {
				HttpHeaders headers = new HttpHeaders();
				headers.set("WWW-Authenticate", "Basic realm=\"Please enter your credentials\"");
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).headers(headers).body("Authentication required");
			}

			// Decode Basic Auth credentials
			String base64Credentials = authHeader.substring("Basic ".length());
			byte[] decodedBytes = Base64.getDecoder().decode(base64Credentials);
			String credentials = new String(decodedBytes, StandardCharsets.UTF_8);
			String[] values = credentials.split(":", 2);

			String username = values[0];
			String password = values[1];

			Authentication authentication = authenticationManager
					.authenticate(new UsernamePasswordAuthenticationToken(username, password));

			if (username.equalsIgnoreCase(this.securityConfig.getDefaultUsername())) {
				System.out.println(defaultCredentialsWarning);
				return ResponseEntity
						.ok(tokenService.generateToken(authentication) + "\n<br>" + defaultCredentialsWarning);
			}

			return ResponseEntity.ok(tokenService.generateToken(authentication));

		} catch (final AuthenticationException e) {
			HttpHeaders headers = new HttpHeaders();
			headers.set("WWW-Authenticate", "Basic realm=\"Invalid credentials\"");
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).headers(headers).body("Invalid credentials");
		}
	}
}