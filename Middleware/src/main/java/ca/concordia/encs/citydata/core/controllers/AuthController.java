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
 * JWT Authentication Implementation
 * Author: Sikandar Ejaz 
 * Date: 18-07-2025
 * 
 * Update: Multi-user authentication added
 * Author: Sikandar Ejaz
 * Last Update: 28-09-2025
 */

@RestController
public class AuthController {

	private final TokenService tokenService;
	private final AuthenticationManager authenticationManager;
	private final SecurityConfig securityConfig;

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

			return ResponseEntity.ok(tokenService.generateToken(authentication));

		} catch (final AuthenticationException e) {
			HttpHeaders headers = new HttpHeaders();
			headers.set("WWW-Authenticate", "Basic realm=\"Invalid credentials\"");
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).headers(headers).body("Invalid credentials");
		}
	}
}