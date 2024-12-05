package software.uncharted.terarium.hmiserver.models.dataservice.dataset;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.databind.JsonNode;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import java.io.Serial;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.annotations.Type;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;
import software.uncharted.terarium.hmiserver.models.TerariumAsset;
import software.uncharted.terarium.hmiserver.models.dataservice.Grounding;

/** Represents a dataset document from TDS */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@TSModel
@Entity
public class Dataset extends TerariumAsset {

	public static final String NC_EXTENSION = ".nc";

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

	@TSOptional
	@JsonAlias("dataset_url")
	@Column(length = 1024)
	private String datasetUrl;

	/** (Optional) List of urls, from which the dataset can be downloaded/fetched. Used for ESGF datasets */
	@TSOptional
	@Type(JsonType.class)
	@Column(columnDefinition = "json")
	private List<String> datasetUrls = new ArrayList<>();

	/** Information regarding the columns that make up the dataset */
	@TSOptional
	@OneToMany(mappedBy = "dataset", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@JsonManagedReference
	private List<DatasetColumn> columns;

	/** (Optional) Unformatted metadata about the dataset */
	@TSOptional
	@Type(JsonType.class)
	@Column(columnDefinition = "json")
	private JsonNode metadata;

	/** (Optional) Source of dataset */
	@TSOptional
	@Column(columnDefinition = "text")
	private String source;

	/** (Optional) Grounding of ontological concepts related to the dataset as a whole */
	@TSOptional
	@OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@JoinColumn(name = "grounding_id")
	private Grounding grounding;

	@Override
	public Dataset clone() {
		final Dataset clone = new Dataset();
		super.cloneSuperFields(clone);

		clone.userId = this.userId;
		clone.esgfId = this.esgfId;
		clone.dataSourceDate = this.dataSourceDate;

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

		if (this.metadata != null) {
			clone.metadata = this.metadata.deepCopy();
		}
		clone.source = this.source;
		if (this.grounding != null) {
			clone.grounding = this.grounding.clone();
		}

		return clone;
	}
}
