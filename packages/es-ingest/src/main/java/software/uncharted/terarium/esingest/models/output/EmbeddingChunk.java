package software.uncharted.terarium.esingest.models.output;

import java.util.UUID;

import lombok.Data;

@Data
public class EmbeddingChunk implements IOutputEmbeddingChunk {

	private UUID id;
	private Embedding embedding;

	public IOutputDocument createPartial() {
		Document partial = new Document();
		partial.setId(id);
		partial.addEmbedding(embedding);
		return partial;
	}

}
