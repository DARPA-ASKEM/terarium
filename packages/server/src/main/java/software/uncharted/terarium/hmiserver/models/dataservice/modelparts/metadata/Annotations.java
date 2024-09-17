package software.uncharted.terarium.hmiserver.models.dataservice.modelparts.metadata;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.AMRSchemaType;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;
import software.uncharted.terarium.hmiserver.models.SupportAdditionalProperties;

@Data
@EqualsAndHashCode(callSuper = true)
@AMRSchemaType
@Accessors(chain = true)
public class Annotations extends SupportAdditionalProperties implements Serializable {

	@Serial
	private static final long serialVersionUID = -2240171862204728842L;

	public Annotations() {
		this.license = null;
		this.authors = new ArrayList();
		this.references = new ArrayList();
		this.timeScale = null;
		this.timeStart = null;
		this.timeEnd = null;
		this.locations = new ArrayList();
		this.pathogens = new ArrayList();
		this.diseases = new ArrayList();
		this.hosts = new ArrayList();
		this.modelTypes = new ArrayList();
	}

	@TSOptional
	private String license;

	@TSOptional
	private List<Author> authors;

	@TSOptional
	private List<String> references;

	@TSOptional
	@JsonProperty("time_scale")
	private String timeScale;

	@TSOptional
	@JsonProperty("time_start")
	private String timeStart;

	@TSOptional
	@JsonProperty("time_end")
	private String timeEnd;

	@TSOptional
	private List<String> locations;

	@TSOptional
	private List<String> pathogens;

	@TSOptional
	private List<String> diseases;

	@TSOptional
	private List<String> hosts;

	@TSOptional
	@JsonProperty("model_types")
	private List<String> modelTypes;

	@Override
	public Annotations clone() {
		final Annotations clone = (Annotations) super.clone();

		clone.license = this.license;
		if (authors != null) {
			clone.authors = new ArrayList<>(authors);
		}

		if (references != null) {
			clone.references = new ArrayList<>(references);
		}

		clone.timeScale = timeScale;
		clone.timeStart = timeStart;
		clone.timeEnd = timeEnd;

		if (locations != null) {
			clone.locations = new ArrayList<>(locations);
		}

		if (pathogens != null) {
			clone.pathogens = new ArrayList<>(pathogens);
		}

		if (diseases != null) {
			clone.diseases = new ArrayList<>(diseases);
		}

		if (hosts != null) {
			clone.hosts = new ArrayList<>(hosts);
		}

		if (modelTypes != null) {
			clone.modelTypes = new ArrayList<>(modelTypes);
		}

		return clone;
	}
}
