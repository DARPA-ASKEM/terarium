package software.uncharted.terarium.hmiserver.models.dataservice.dataset;

import java.sql.Timestamp;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonAlias;

import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;
import software.uncharted.terarium.hmiserver.models.dataservice.Grounding;

/**
 * Represents a dataset document from TDS
 */
@Data
@Accessors(chain = true)
@TSModel
public class Dataset {

	/**
	 * Universally unique identifier for the dataset
	 **/
	@TSOptional
	private String id; // Is this a UUID?

	/**
	 * Timestamp of when the dataset was created
	 **/
	// TODO: This should be "Instant" but there is an issue and a time crunch
	// here...
	@TSOptional
	private Object timestamp;

	/**
	 * Username of the user who created the dataset
	 **/
	@TSOptional
	private String username;

	/**
	 * Name of the dataset
	 **/
	private String name;

	/**
	 * (Optional) textual description of the dataset
	 **/
	@TSOptional
	private String description;

	/**
	 * (Optional) data source date
	 **/
	@TSOptional
	@JsonAlias("data_source_date")
	private Timestamp dataSourceDate;

	/**
	 * (Optional) list of file names associated with the dataset
	 **/
	@TSOptional
	@JsonAlias("file_names")
	private List<String> fileNames;

	/**
	 * (Optional) Url from which the dataset can be downloaded/fetched
	 **/
	@TSOptional
	@JsonAlias("dataset_url")
	private String datasetUrl;

	/**
	 * Information regarding the columns that make up the dataset
	 **/
	@TSOptional
	private List<DatasetColumn> columns;

	/**
	 * (Optional) Unformatted metadata about the dataset
	 **/
	@TSOptional
	private Object metadata;

	/**
	 * (Optional) Source of dataset
	 **/
	@TSOptional
	private String source;

	/**
	 * (Optional) Grounding of ontological concepts related to the dataset as a
	 * whole
	 **/
	@TSOptional
	private Grounding grounding;
}
