package software.uncharted.terarium.ingest.input.model;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.Data;
import software.uncharted.terarium.ingest.input.IAssetInput;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Model implements IAssetInput {

	public UUID getId() {
		return null;
	}

	public JsonNode getAsset() {
		return null;
	}
}
