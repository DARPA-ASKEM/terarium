package software.uncharted.terarium.esingest.models.input.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.Data;
import software.uncharted.terarium.esingest.models.input.IInputDocument;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ModelAMR implements IInputDocument {

	private String id;
	private JsonNode header;
	private JsonNode model;
	private JsonNode semantics;
	private JsonNode metadata;
}
