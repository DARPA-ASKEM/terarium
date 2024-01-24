package software.uncharted.terarium.hmiserver.models.dataservice.regnet;

import java.util.List;

import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;
import software.uncharted.terarium.hmiserver.models.SupportAdditionalProperties;

@Data
@Accessors(chain = true)
@TSModel
public class RegNetModel implements SupportAdditionalProperties {
	private List<RegNetVertex> vertices;
	private List<RegNetEdge> edges;

	@TSOptional
	private List<RegNetParameter> parameters;
}
