package software.uncharted.terarium.hmiserver.models.dataservice.dataset;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;

import java.util.List;

/**
 * Represents a dataset document from TDS
 */
@Data
@Accessors(chain = true)
@TSModel
public class Dataset {

	/** Universally unique identifier for the dataset **/
	@TSOptional
	private String id; //Is this a UUID?

	/** Name of the dataset **/
	private String name;

	/** (Optional) textual description of the dataset **/
	@TSOptional
	private String description;

	/** (Optional) Url from which the dataset can be downloaded/fetched **/
	@TSOptional
	@JsonAlias("data_url")
	private String url;

	/** Information regarding the columns that make up the dataset **/
	@TSOptional
	private List<DatasetColumn> columns;

	/** (Optional) Unformatted metadata about the dataset **/
	@TSOptional
	private Object metadata;

	/** (Optional) Source of dataset **/
	@TSOptional
	private String source;

	/** (Optional) Grounding of ontological concepts related to the dataset as a whole **/
	@TSOptional
	private Grounding grounding;
}
