package software.uncharted.terarium.hmiserver.models.dataservice;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.json.bind.annotation.JsonbProperty;
import java.io.Serializable;

@Data
@Accessors(chain = true)
public class OntologyConcept implements Serializable {

	private String id;

	private String curie;

	private TaggableType type;

	@JsonbProperty("object_id")
	private Long objectId;

	private OntologicalField status;
}
