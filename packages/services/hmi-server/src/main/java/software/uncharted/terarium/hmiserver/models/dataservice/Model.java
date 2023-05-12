package software.uncharted.terarium.hmiserver.models.dataservice;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import lombok.Data;
import lombok.experimental.Accessors;

package software.uncharted.terarium.hmiserver.models.dataservice.ModelMetadata;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
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
}
