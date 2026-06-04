package ca.concordia.encs.citydata.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.core.io.ClassPathResource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import ca.concordia.encs.citydata.core.exceptions.AccessDeniedException;
import ca.concordia.encs.citydata.core.exceptions.MetadataException;
import ca.concordia.encs.citydata.core.model.DatasetType;

/**
 * Handles authorisation checks and dataset retrieval.
 * 
 * @author Sikandar Ejaz
 * @since 2026-06-01
 */

@Service
public class DatasetAccessService {

	public String getDatasetContent(DatasetType type) {
		String username = resolveCurrentUsername();
		checkAuthorisation(username, type);
		return readClasspathFile(type.getDatasetPath());
	}

	public boolean isAuthorised(String username, DatasetType type) {
		List<String> authorisedUsers = loadAuthorisedUsers(type);
		return authorisedUsers.contains(username.trim().toLowerCase());
	}

	public void checkAuthorisationForPath(String username, String metadataPath) {
		List<String> lines = readClasspathFileLines(metadataPath);

		if (lines.isEmpty()) {
			throw new MetadataException("Metadata file is empty: " + metadataPath);
		}

		List<String> authorisedUsers = lines.stream().skip(1).map(line -> line.trim().toLowerCase())
				.filter(line -> !line.isEmpty()).collect(Collectors.toList());

		if (!authorisedUsers.contains(username.trim().toLowerCase())) {
			throw new AccessDeniedException(username, metadataPath);
		}
	}

	/**
	 * Resolves the username of the currently authenticated principal from the Spring Security context.
	 */

	String resolveCurrentUsername() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null || !auth.isAuthenticated() || auth.getName() == null) {
			throw new AccessDeniedException("anonymous", "any");
		}
		return auth.getName();
	}

	/**
	 * Throws AccessDeniedException if the user is not in the metadata file.
	 */

	private void checkAuthorisation(String username, DatasetType type) {
		if (!isAuthorised(username, type)) {
			throw new AccessDeniedException(username, type.name());
		}
	}

	public List<String> loadAuthorisedUsers(DatasetType type) {
		List<String> lines = readClasspathFileLines(type.getMetadataPath());

		if (lines.isEmpty()) {
			throw new MetadataException("Metadata file is empty: " + type.getMetadataPath());
		}

		// Validate the type label on line 1
		String firstLine = lines.get(0).trim();
		if (!firstLine.equalsIgnoreCase(type.getMetadataLabel())) {
			throw new MetadataException(
					String.format("Metadata file '%s' has unexpected type label '%s' (expected '%s').",
							type.getMetadataPath(), firstLine, type.getMetadataLabel()));
		}

		// Everything after the first line is a username (lowercased for comparison)
		return lines.stream().skip(1).map(line -> line.trim().toLowerCase()).filter(line -> !line.isEmpty())
				.collect(Collectors.toList());
	}

	/**
	 * Reads a classpath resource and returns its content as a single string.
	 */

	private String readClasspathFile(String classpathPath) {
		try {
			ClassPathResource resource = new ClassPathResource(classpathPath);
			try (BufferedReader reader = new BufferedReader(
					new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
				return reader.lines().collect(Collectors.joining(System.lineSeparator()));
			}
		} catch (IOException e) {
			throw new MetadataException("Failed to read file: " + classpathPath, e);
		}
	}

	/**
	 * Reads a classpath resource and returns its lines as a list.
	 */

	private List<String> readClasspathFileLines(String classpathPath) {
		try {
			ClassPathResource resource = new ClassPathResource(classpathPath);
			try (BufferedReader reader = new BufferedReader(
					new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
				return reader.lines().collect(Collectors.toList());
			}
		} catch (IOException e) {
			throw new MetadataException("Metadata file not found on classpath: " + classpathPath, e);
		}
	}
}
