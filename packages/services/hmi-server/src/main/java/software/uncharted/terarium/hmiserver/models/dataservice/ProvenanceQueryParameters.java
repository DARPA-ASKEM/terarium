package software.uncharted.terarium.hmiserver.models.dataservice;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@Accessors(chain = true)
public class ProvenanceQueryParameters implements Serializable {

	@JsonProperty("root_id")
	private Number root_id;

	@JsonProperty("root_type")
	private String root_type; // use ProvenanceType

	@JsonProperty("user_id")
	private Number user_id;

}
