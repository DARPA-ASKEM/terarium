package software.uncharted.terarium.hmiserver.models.dataservice;

import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.ModelMetadata;
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

	private String name;

	@JsonSetter(nulls = Nulls.SKIP)
	private String description = "";

	private String model_version;

	private String schema;

	private Map<String, Object> model;

	private ModelSemantics semantics;

	private ModelMetadata metadata;

	// FIXME: deprecated, remove
	@TSOptional
	private ModelContent content;
}
