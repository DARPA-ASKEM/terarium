package software.uncharted.terarium.hmiserver.models.dataservice.dataset;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.annotations.Type;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonBackReference;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;
import software.uncharted.terarium.hmiserver.models.BaseEntity;
import software.uncharted.terarium.hmiserver.models.dataservice.Grounding;

/** Represents a column in a dataset */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TSModel
@Entity
public class DatasetColumn extends BaseEntity {

	/** Name of the column */
	@Column(length = 255)
	private String name;

	@TSOptional
	@ManyToOne
	@JoinColumn(name = "dataset_id")
	@JsonBackReference
	private Dataset dataset;

	/**
	 * Datatype. One of: unknown, boolean, string, char, integer, int, float,
	 * double, timestamp, datetime, date, time
	 */
	@JsonAlias("data_type")
	@Enumerated(EnumType.STRING)
	private ColumnType dataType;

	/** (Optional) String that describes the formatting of the value */
	@TSOptional
	@JsonAlias("format_str")
	@Column(length = 255)
	private String formatStr;

	/** Column annotations from the MIT data profiling tool */
	@Column(columnDefinition = "text")
	@Type(JsonType.class)
	private List<String> annotations;

	/** (Optional) Unformatted metadata about the dataset */
	@TSOptional
	@Column(columnDefinition = "text")
	@Type(JsonType.class)
	private Map<String, Object> metadata;

	/** (Optional) Grounding of ontological concepts related to the column */
	@TSOptional
	@OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@JoinColumn(name = "grounding_id")
	private Grounding grounding;

	@TSOptional
	@Column(columnDefinition = "text")
	private String description;

	public void updateMetadata(final Map<String, Object> metadata) {
		if (this.metadata == null) {
			this.metadata = metadata;
		} else {
			this.metadata.putAll(metadata);
		}
	}

	@Override
	public DatasetColumn clone() {
		final DatasetColumn clone = new DatasetColumn();

		clone.name = this.name;
		clone.dataType = this.dataType;
		clone.formatStr = this.formatStr;
		if (this.annotations != null) {
			clone.annotations = new ArrayList<>();
			clone.annotations.addAll(this.annotations);
		}

		if (this.metadata != null) {
			clone.metadata = new HashMap<>();
			clone.metadata.putAll(this.metadata);
		}

		if (this.grounding != null)
			clone.grounding = this.grounding.clone();

		clone.description = this.description;

		return clone;
	}

	public enum ColumnType {
		@JsonAlias("unknown")
		UNKNOWN,
		@JsonAlias("boolean")
		BOOLEAN,
		@JsonAlias("string")
		STRING,
		@JsonAlias("char")
		CHAR,
		@JsonAlias("integer")
		INTEGER,
		@JsonAlias("int")
		INT,
		@JsonAlias("float")
		FLOAT,
		@JsonAlias("double")
		DOUBLE,
		@JsonAlias("timestamp")
		TIMESTAMP,
		@JsonAlias("datetime")
		DATETIME,
		@JsonAlias("date")
		DATE,
		@JsonAlias("time")
		TIME
	}
}
