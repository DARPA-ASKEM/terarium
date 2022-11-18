package software.uncharted.terarium.hmiserver.models.dataservice;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.json.bind.annotation.JsonbProperty;
import java.io.Serializable;

@Data
@Accessors(chain = true)
public class OntologyConcept implements Serializable {

	private String id;

	@JsonbProperty("term_id")
	private String termId;

	private TaggableType type;

	@JsonbProperty("obj_id")
	private Long objId;

	private OntologicalField status;
}
