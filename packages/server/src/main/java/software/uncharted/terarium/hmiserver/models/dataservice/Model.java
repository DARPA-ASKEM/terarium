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
import java.util.Map;

@Data
@Accessors(chain = true)
@TSModel
public class Model implements Serializable {
	private String id;

	private ModelHeader header;

	private Map<String, Object> model;

	@TSOptional
	private Object properties;

	@TSOptional
	private ModelSemantics semantics;

	@TSOptional
	private ModelMetadata metadata;
}
