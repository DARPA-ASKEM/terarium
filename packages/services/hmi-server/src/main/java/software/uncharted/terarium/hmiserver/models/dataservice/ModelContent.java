package software.uncharted.terarium.hmiserver.models.dataservice;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.models.petrinet.Species;

import java.io.Serializable;
import java.util.*;

@Data
@Accessors(chain = true)
public class ModelContent implements Serializable {

	@JsonAlias("S")
	@JsonSetter("S")
	private List<Species> S;

	@JsonAlias("T")
	@JsonSetter("T")
	private List<Map<String, Optional<String>>> T;

	@JsonAlias("I")
	@JsonSetter("I")
	private List<Map<String, Number>> I;

	@JsonAlias("O")
	@JsonSetter("O")
	private List<Map<String, Number>> O;
}
