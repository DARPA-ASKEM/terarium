package software.uncharted.terarium.hmiserver.models.dataservice.modelparts.metadata;

import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;

import java.util.List;

@Data
@Accessors(chain = true)
public class Annotations {
	@TSOptional
	@JsonSetter(nulls = Nulls.SKIP)
	private String license;

	@TSOptional
	private List<String> authors;

	@TSOptional
	private List<String> references;

	@TSOptional
	@JsonSetter(nulls = Nulls.SKIP)
	private String time_scale;

	@TSOptional
	@JsonSetter(nulls = Nulls.SKIP)
	private String time_start;

	@TSOptional
	@JsonSetter(nulls = Nulls.SKIP)
	private String time_end;

	@TSOptional
	private List<String> locations;

	@TSOptional
	private List<String> pathogens;

	@TSOptional
	private List<String> diseases;

	@TSOptional
	private List<String> hosts;

	@TSOptional
	private List<String> model_types;
}
