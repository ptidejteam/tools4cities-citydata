package ca.concordia.encs.citydata.core.controllers;

import java.util.Map;

import org.springframework.http.HttpStatus;
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
