package software.uncharted.terarium.hmiserver.models.dataservice.dataset;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.io.Serial;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;
import software.uncharted.terarium.hmiserver.models.TerariumAsset;
import software.uncharted.terarium.hmiserver.models.dataservice.Grounding;
import software.uncharted.terarium.hmiserver.models.dataservice.project.Project;

/** Represents a dataset document from TDS */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@TSModel
@Entity
public class Dataset extends TerariumAsset {

	@Serial
	private static final long serialVersionUID = 6927286281160755696L;

	/** UserId of the user who created the dataset */
	@TSOptional
	@Column(length = 255)
	private String userId;

	/** ESGF id of the dataset. This will be null for datasets that are not from ESGF */
	@TSOptional
	@Column(length = 255)
	private String esgfId;

	/** (Optional) data source date */
	@TSOptional
	@JsonAlias("data_source_date")
	@Column(columnDefinition = "TIMESTAMP WITH TIME ZONE")
	private Timestamp dataSourceDate;

	/** (Optional) list of file names associated with the dataset */
	@TSOptional
	@JsonAlias("file_names")
	@ElementCollection
	@Column(length = 1024)
	private List<String> fileNames;

	@ManyToOne
	@JoinColumn(name = "project_id")
	@JsonBackReference
	@TSOptional
	private Project project;

	@TSOptional
	@JsonAlias("dataset_url")
	@Column(length = 1024)
	private String datasetUrl;

	/** (Optional) List of urls from which the dataset can be downloaded/fetched. Used for ESGF datasets */
	@TSOptional
	@Column(length = 1024)
	@ElementCollection
	private List<String> datasetUrls;

	/** Information regarding the columns that make up the dataset */
	@TSOptional
	@JdbcTypeCode(SqlTypes.JSON)
	private List<DatasetColumn> columns;

	/** (Optional) Unformatted metadata about the dataset */
	@TSOptional
	@JdbcTypeCode(SqlTypes.JSON)
	@Column(columnDefinition = "text")
	private JsonNode metadata;

	/** (Optional) Source of dataset */
	@TSOptional
	@Column(columnDefinition = "text")
	private String source;

	/** (Optional) Grounding of ontological concepts related to the dataset as a whole */
	@TSOptional
	@JdbcTypeCode(SqlTypes.JSON)
	private Grounding grounding;

	@Override
	public Dataset clone() {
		final Dataset clone = new Dataset();
		super.cloneSuperFields(clone);

		clone.userId = this.userId;
		clone.esgfId = this.esgfId;
		clone.dataSourceDate = this.dataSourceDate;
		if (fileNames != null) {
			clone.fileNames = new ArrayList<>();
			clone.fileNames.addAll(fileNames);
		}
		clone.datasetUrl = this.datasetUrl;
		if (datasetUrls != null) {
			clone.datasetUrls = new ArrayList<>();
			clone.datasetUrls.addAll(datasetUrls);
		}

		if (columns != null) {
			clone.columns = new ArrayList<>();
			for (final DatasetColumn column : columns) {
				clone.columns.add(column.clone());
			}
		}

		if(this.metadata!= null)
			clone.metadata = this.metadata.deepCopy();
		clone.source = this.source;
		if(this.grounding != null)
			clone.grounding = this.grounding.clone();

		return clone;
	}
}
