package software.uncharted.terarium.hmiserver.models.dataservice;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@Accessors(chain = true)
public class OntologyConcept implements Serializable {

	private String id;

	private String curie;

	private TaggableType type;

	@JsonProperty("object_id")
	private Long objectId;

	private OntologicalField status;
}
