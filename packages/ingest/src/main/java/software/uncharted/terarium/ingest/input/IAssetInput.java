package software.uncharted.terarium.ingest.input;

import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;

public interface IAssetInput {

	public enum AssetType {
		DOCUMENT, MODEL, DATASET
	}

	UUID getId();

	JsonNode getAsset();

}
