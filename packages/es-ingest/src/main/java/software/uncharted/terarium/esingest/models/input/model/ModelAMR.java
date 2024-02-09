package software.uncharted.terarium.esingest.models.input.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import lombok.Data;
import software.uncharted.terarium.esingest.models.input.IInputDocument;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonDeserialize(using = ModelAMRDeserializer.class)
public class ModelAMR implements IInputDocument {

	private String id;
	private JsonNode amr;
}
