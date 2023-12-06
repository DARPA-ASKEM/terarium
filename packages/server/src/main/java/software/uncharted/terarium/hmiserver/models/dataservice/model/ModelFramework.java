package software.uncharted.terarium.hmiserver.models.dataservice.model;

import java.io.Serializable;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.experimental.Accessors;

@Entity
@Data
@Accessors(chain = true)
public class ModelFramework implements Serializable {

	@Id
	private String name;

	private String version;

	private String semantics;
}
