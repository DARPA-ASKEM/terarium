package software.uncharted.terarium.hmiserver.models.dataservice.regnet;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;
import software.uncharted.terarium.hmiserver.models.SupportAdditionalProperties;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TSModel
public class RegNetModel extends SupportAdditionalProperties implements Serializable {

	@Serial
	private static final long serialVersionUID = -3141762199664068622L;

	private List<RegNetVertex> vertices;

	private List<RegNetEdge> edges;

	@TSOptional
	private List<RegNetParameter> parameters;
}
