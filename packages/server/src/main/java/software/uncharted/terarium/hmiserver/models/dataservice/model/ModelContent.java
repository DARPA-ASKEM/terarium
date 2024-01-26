package software.uncharted.terarium.hmiserver.models.dataservice.model;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.models.SupportAdditionalProperties;
import software.uncharted.terarium.hmiserver.models.petrinet.Species;

@Data
@Accessors(chain = true)
public class ModelContent implements SupportAdditionalProperties, Serializable {

	@JsonProperty("S")
	private List<Species> S;

	@JsonProperty("T")
	private List<Map<String, Optional<String>>> T;

	@JsonProperty("I")
	private List<Map<String, Number>> I;

	@JsonProperty("O")
	private List<Map<String, Number>> O;

	private Object metadata;
}
