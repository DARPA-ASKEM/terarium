package software.uncharted.terarium.hmiserver.models.dataservice;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.json.bind.annotation.JsonbProperty;
import java.io.Serializable;

@Data
@Accessors(chain = true)
public class ProvenanceQueryParameters implements Serializable {

	@JsonbProperty("root_id")
	private Number root_id;

	@JsonbProperty("root_type")
	private String root_type; // use ProvenanceType

	@JsonbProperty("user_id")
	private Number user_id;

	// private String curie;
}
