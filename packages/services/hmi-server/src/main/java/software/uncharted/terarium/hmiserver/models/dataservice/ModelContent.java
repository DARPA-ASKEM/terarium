package software.uncharted.terarium.hmiserver.models.dataservice;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.models.Ontology;
import software.uncharted.terarium.hmiserver.models.petrinet.Species;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.function.BiFunction;

@Data
@Accessors(chain = true)
public class ModelContent implements Serializable {

	@JsonAlias("S")
	private List<Species> species;

	@JsonAlias("T")
	private List<Map<String, Optional<String>>> transitions;

	@JsonAlias("I")
	private List<Map<String, Number>> input;

	@JsonAlias("O")
	private List<Map<String, Number>> output;
}
