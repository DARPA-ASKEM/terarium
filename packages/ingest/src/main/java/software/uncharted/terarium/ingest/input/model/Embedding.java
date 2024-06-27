package software.uncharted.terarium.ingest.input.model;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import software.uncharted.terarium.ingest.input.IEmbeddingInput;
import software.uncharted.terarium.ingest.models.TerariumAssetEmbeddings;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Embedding implements IEmbeddingInput {

	public UUID getId() {
		return null;
	}

	public TerariumAssetEmbeddings getEmbedding() {
		return null;
	};
}
