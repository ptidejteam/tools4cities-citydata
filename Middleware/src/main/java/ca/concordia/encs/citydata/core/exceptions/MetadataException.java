package ca.concordia.encs.citydata.core.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a metadata file cannot be found on the classpath, is empty,
 * or has a first line that does not match the expected dataset type label.
 * 
 * @author Sikandar Ejaz
 * @since 2026-06-01
 */

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class MetadataException extends RuntimeException {

	public MetadataException(String message) {
		super(message);
	}

	public MetadataException(String message, Throwable cause) {
		super(message, cause);
	}
}
