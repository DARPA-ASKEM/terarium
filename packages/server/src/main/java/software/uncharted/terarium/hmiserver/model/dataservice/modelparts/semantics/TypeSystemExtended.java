package software.uncharted.terarium.hmiserver.model.dataservice.modelparts.semantics;
import software.uncharted.terarium.hmiserver.model.dataservice.modelparts.ModelSemantics;
import software.uncharted.terarium.hmiserver.model.dataservice.modelparts.ModelMetadata;

import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;

import java.util.Map;
import com.fasterxml.jackson.annotation.Nulls;
import com.fasterxml.jackson.annotation.JsonSetter;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;
import java.io.Serializable;

@Data
@Accessors(chain = true)
@TSModel
public class TypeSystemExtended implements Serializable{
	private String name;

	@JsonSetter(nulls = Nulls.SKIP)
	private String description = "";

	private String schema;

	private String model_version;

	private Map<String, Object> model;

	@TSOptional
	private Object properties;

	@TSOptional
	private ModelSemantics semantics;

	@TSOptional
	private ModelMetadata metadata;


}
