package software.uncharted.terarium.hmiserver.models.dataservice.model;

import java.io.Serial;
import java.io.Serializable;

import java.util.Map;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;
import software.uncharted.terarium.hmiserver.models.SupportAdditionalProperties;
import software.uncharted.terarium.hmiserver.models.dataservice.TerariumAsset;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.ModelHeader;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.ModelMetadata;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.ModelSemantics;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@TSModel
public class Model extends TerariumAsset implements SupportAdditionalProperties, Serializable {

	@Serial
	private static final long serialVersionUID = 398195277271188277L;

	private ModelHeader header;

	@TSOptional
	private String userId;

	private Map<String, Object> model;

	private String modelCard;

	@TSOptional
	private Object properties;

	@TSOptional
	private ModelSemantics semantics;

	@TSOptional
	private ModelMetadata metadata;
}
