package software.uncharted.terarium.hmiserver.models.dataservice;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@Accessors(chain = true)
public class ProvenanceQueryParameters implements Serializable {

	@JsonAlias("root_id")
	private Number root_id;

	@JsonAlias("root_type")
	private String root_type; // use ProvenanceType

	@JsonAlias("user_id")
	private Number user_id;

	// private String curie;
}
