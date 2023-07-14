package software.uncharted.terarium.hmiserver.models.dataservice.modelparts.semantics;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.ModelSemantics;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.ModelMetadata;

import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;

import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.Nulls;
import com.fasterxml.jackson.annotation.JsonSetter;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;
import java.io.Serializable;

@Data
@Accessors(chain = true)
@TSModel
public class TypeSystem  implements Serializable{
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
