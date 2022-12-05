package software.uncharted.terarium.hmiserver.models.dataservice;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Data
@Accessors(chain = true)
public class Project implements Serializable {

	private String id;

	private String name;

	private String description;

	private Instant timestamp;

	private Boolean active;

	private Concept concept;

	private Map<String, List<Long>> assets;
}
