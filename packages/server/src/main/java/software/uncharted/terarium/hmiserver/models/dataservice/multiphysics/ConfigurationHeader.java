package software.uncharted.terarium.hmiserver.models.dataservice.multiphysics;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import java.io.Serializable;
import java.util.Map;

@Data
@Accessors(chain = true)
@TSModel
public class ConfigurationHeader implements Serializable {
	private String id;
	private String description;
	private String name;

	@JsonAlias("parentContext")
	private String parentContext;
}
