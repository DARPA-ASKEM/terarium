package software.uncharted.terarium.hmiserver.models.dataservice.modelparts.metadata;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serial;
import java.io.Serializable;
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

	@TSOptional
	private String license;

	@TSOptional
	private List<String> authors;

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
}
