package software.uncharted.terarium.hmiserver.models.dataservice;

import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.ModelMetadata;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Accessors(chain = true)
public class Model implements Serializable {

	private String id;

	private String name;

	@JsonSetter(nulls = Nulls.SKIP)
	private String description = "";

	private String schema;

	private Map<String, Object> model;

	private Map<String, Object> properties;

	private ModelMetadata metadata;

	// FIXME: deprecated, remove
	private ModelContent content;
}
