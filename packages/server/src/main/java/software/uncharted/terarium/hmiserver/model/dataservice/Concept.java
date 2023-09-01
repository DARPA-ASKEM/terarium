package software.uncharted.terarium.hmiserver.model.dataservice;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@Accessors(chain = true)
public class Concept implements Serializable {

	private String id;

	private String curie;

	private Assets.AssetType type;

	@JsonProperty("object_id")
	private String objectID;

	private OntologicalField status;
}
