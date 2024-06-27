package software.uncharted.terarium.ingest.input.epi;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import software.uncharted.terarium.ingest.input.IEmbeddingInput;
import software.uncharted.terarium.ingest.models.TerariumAssetEmbeddings;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Embedding implements IEmbeddingInput {

	@JsonProperty("doc_id")
	private String id;

	@JsonProperty("uuid")
	private String embeddingChunkId;

	private long[] spans;
	private String title;
	private List<String> doi;
	private double[] embedding;

	public UUID getId() {
		return null;
	}

	public TerariumAssetEmbeddings getEmbedding() {
		return null;
	};
}
