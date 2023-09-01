package software.uncharted.terarium.hmiserver.models.dataservice;

import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.ModelMetadata;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.ModelHeader;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.ModelSemantics;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Accessors(chain = true)
@TSModel
public class Model implements Serializable {
	private String id;

	// To be removed
	// See: https://github.com/DARPA-ASKEM/Model-Representations/commit/f2c90f16c6c3865f71d1e727e15bc2f0b1f5ec58
	@deprecated
	private String name;

	@deprecated
	@JsonSetter(nulls = Nulls.SKIP)
	private String description = "";

	@deprecated
	@TSOptional
	private String model_version;

	@deprecated
	private String schema;

	@deprecated
	@TSOptional
	private String schema_name;
	// End

	private ModelHeader header;

	private Map<String, Object> model;

	@TSOptional
	private Object properties;

	@TSOptional
	private ModelSemantics semantics;

	@TSOptional
	private ModelMetadata metadata;

}
