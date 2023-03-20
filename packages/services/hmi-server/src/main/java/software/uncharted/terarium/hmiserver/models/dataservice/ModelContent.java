package software.uncharted.terarium.hmiserver.models.dataservice;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.models.Ontology;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.function.BiFunction;

@Data
@Accessors(chain = true)
public class ModelContent implements Serializable {

	@JsonAlias("S")
	private List<Map<String, Object>> species;
	/* Example:
		{
			"sname": "S",
			"mira_ids": "[('identity', 'ido:0000514')]",
			"mira_context": "[]"
		}
	 */

	@JsonAlias("T")
	private List<Map<String, Optional<String>>> transitions;

	@JsonAlias("I")
	private List<Map<String, Number>> input;

	@JsonAlias("O")
	private List<Map<String, Number>> output;

	public void curieResolver() {
		ObjectMapper ontologiesMapper = new ObjectMapper();
		BiFunction<String, Object, Object> resolver = (key, value) -> {
			if (!(value instanceof String)) return value;
			try {
				return ontologiesMapper.readValue((String) value, Ontology.class);
			} catch (JsonProcessingException e) {
				throw new RuntimeException(e);
			}
		};

		for (Map<String, Object> specie : species) {
			// for now only check for the keys 'mira_ids' and 'mira_context'
			specie.computeIfPresent("mira_ids", resolver);
			specie.computeIfPresent("mira_context", resolver);
		}
	}
}
