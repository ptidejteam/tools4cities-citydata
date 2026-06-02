package ca.concordia.encs.citydata.core.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when an authenticated user attempts to access a dataset they are
 * not listed in the corresponding metadata file.
 * 
 * @author Sikandar Ejaz
 * @since 2026-06-01
 */

@ResponseStatus(HttpStatus.FORBIDDEN)
public class AccessDeniedException extends RuntimeException {

	public AccessDeniedException(String username, String datasetType) {
		super(String.format("User '%s' is not authorised to access the %s dataset.", username, datasetType));
	}
}
