package software.uncharted.terarium.hmiserver.models.dataservice.provenance;

import java.io.Serializable;
import java.util.UUID;

import org.springframework.data.neo4j.core.schema.Id;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ProvenanceNode implements Serializable {

	@Id
	private UUID id;

	private ProvenanceType type;

	@JsonProperty("uuid")
	String getUuid() {
		String label = type.toString().toLowerCase();
		String path = label + "s/" + id;

		switch (label) {
			case "intermediate":
			case "modelparameter":
				path = "model/" + label + "s/" + id;
				break;
			case "simulationrun":
				path = "simulations/runs/" + id;
				break;
			case "plan":
				path = "simulations/plans/" + id;
				break;
			case "simulationparameter":
				path = "simulations/simulationparameters/" + id;
				break;
			case "modelrevisions":
				path = null;
				break;
		}

		return path;
	}
}
