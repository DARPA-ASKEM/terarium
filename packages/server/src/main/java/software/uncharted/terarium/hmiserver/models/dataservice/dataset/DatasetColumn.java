package software.uncharted.terarium.hmiserver.models.dataservice.dataset;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.experimental.Accessors;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;
import software.uncharted.terarium.hmiserver.models.dataservice.Grounding;

/** Represents a column in a dataset */
@Data
@Accessors(chain = true)
@TSModel
public class DatasetColumn {

	/** Name of the column */
	@Column(length = 255)
	private String name;

	/**
	 * Datatype. One of: unknown, boolean, string, char, integer, int, float, double, timestamp, datetime, date, time
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
	private List<String> annotations;

	/** (Optional) Unformatted metadata about the dataset */
	@TSOptional
	@JdbcTypeCode(SqlTypes.JSON)
	private Map<String, Object> metadata;

	/** (Optional) Grounding of ontological concepts related to the column */
	@TSOptional
	@JdbcTypeCode(SqlTypes.JSON)
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

		clone.grounding = this.grounding.clone();

		clone.description = this.description;

		return clone;
	}

	enum ColumnType {
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
