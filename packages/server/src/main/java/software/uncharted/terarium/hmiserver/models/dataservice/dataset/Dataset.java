package software.uncharted.terarium.hmiserver.models.dataservice.dataset;

import java.io.Serial;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAlias;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;
import software.uncharted.terarium.hmiserver.models.TerariumAsset;
import software.uncharted.terarium.hmiserver.models.dataservice.Grounding;

/**
 * Represents a dataset document from TDS
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@TSModel
public class Dataset extends TerariumAsset {

	@Serial
	private static final long serialVersionUID = 6927286281160755696L;
	/**
	 * UserId of the user who created the dataset
	 **/
	@TSOptional
	private String userId;

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
	private Map<String, Object> metadata;

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
