package software.uncharted.terarium.hmiserver.models.dataservice.regnet;

import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;

import java.util.List;

@Data
@Accessors(chain = true)
public class RegNetModel {
	private List<RegNetVertex> vertices;
	private List<RegNetEdge> edges;

	@TSOptional
	private List<RegNetParameter> parameters;
}
