package ca.concordia.encs.citydata.core.controllers;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ca.concordia.encs.citydata.core.exceptions.AccessDeniedException;
import ca.concordia.encs.citydata.core.exceptions.MetadataException;
import ca.concordia.encs.citydata.core.model.DatasetType;
import ca.concordia.encs.citydata.services.DatasetAccessService;

/**
 * REST controller providing access to the three dataset tiers.
 * 
 * @author Sikandar Ejaz
 * @since 2026-06-01
 */

@RestController
@RequestMapping("/api/datasets")
public class DatasetController {

	private final DatasetAccessService datasetAccessService;

	public DatasetController(DatasetAccessService datasetAccessService) {
		this.datasetAccessService = datasetAccessService;
	}

	@GetMapping("/list")
	public ResponseEntity<String> listDatasets() {
		try {
			var resource = new ClassPathResource("DATA_SOURCES.md");
			String content = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
			return ResponseEntity.ok().contentType(MediaType.TEXT_PLAIN).body(content);
		} catch (IOException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Could not load DATA_SOURCES.md from classpath.");
		}
	}

	@GetMapping("/public")
	public ResponseEntity<Map<String, String>> getPublicDataset() {
		return fetchDataset(DatasetType.PUBLIC);
	}

	@GetMapping("/protected")
	public ResponseEntity<Map<String, String>> getProtectedDataset() {
		return fetchDataset(DatasetType.PROTECTED);
	}

	@GetMapping("/private")
	public ResponseEntity<Map<String, String>> getPrivateDataset() {
		return fetchDataset(DatasetType.PRIVATE);
	}

	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<Map<String, String>> handleAccessDenied(AccessDeniedException ex) {
		return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", ex.getMessage()));
	}

	@ExceptionHandler(MetadataException.class)
	public ResponseEntity<Map<String, String>> handleMetadataError(MetadataException ex) {
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", ex.getMessage()));
	}

	private ResponseEntity<Map<String, String>> fetchDataset(DatasetType type) {
		String content = datasetAccessService.getDatasetContent(type);
		return ResponseEntity.ok(Map.of("content", content));
	}
}
