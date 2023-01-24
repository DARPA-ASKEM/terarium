package software.uncharted.terarium.hmiserver.models.dataservice;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.json.bind.annotation.JsonbProperty;
import java.io.Serializable;

@Data
@Accessors(chain = true)
public class Concept implements Serializable {

	private String id;

	private String curie;

	private ResourceType type;

	@JsonbProperty("object_id")
	private String objectID;

	private OntologicalField status;
}
