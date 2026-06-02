package ca.concordia.encs.citydata.core.model;

/**
 * Represents the three tiers of dataset access control.
 * Each type maps to a corresponding metadata file and dataset CSV.
 * 
 * @author Sikandar Ejaz
 * @since 2026-06-01
 */

public enum DatasetType {

	PUBLIC("Public", "public_metadata.txt", "public_dataset.csv"),
	PROTECTED("Protected", "protected_metadata.txt", "protected_dataset.csv"),
	PRIVATE("Private", "private_metadata.txt", "private_dataset.csv");

	/** The exact label expected on the first line of the metadata file. */
	private final String metadataLabel;

	/** Classpath-relative path to the metadata file (under src/test/resources). */
	private final String metadataPath;

	/** Classpath-relative path to the dataset CSV (under src/test/resources). */
	private final String datasetPath;

	DatasetType(String metadataLabel, String metadataPath, String datasetPath) {
		this.metadataLabel = metadataLabel;
		this.metadataPath = metadataPath;
		this.datasetPath = datasetPath;
	}

	public String getMetadataLabel() {
		return metadataLabel;
	}

	public String getMetadataPath() {
		return metadataPath;
	}

	public String getDatasetPath() {
		return datasetPath;
	}
}
