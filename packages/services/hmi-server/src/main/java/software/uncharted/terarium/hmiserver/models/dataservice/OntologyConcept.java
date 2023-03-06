package software.uncharted.terarium.hmiserver.models.dataservice;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@Accessors(chain = true)
public class OntologyConcept implements Serializable {

	private String id;

	private String curie;

	private TaggableType type;

	@JsonAlias("object_id")
	private Long objectId;

	private OntologicalField status;
}
