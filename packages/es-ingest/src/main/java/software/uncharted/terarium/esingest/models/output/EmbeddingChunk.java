package software.uncharted.terarium.esingest.models.output;

import java.io.Serializable;
import java.util.UUID;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class EmbeddingChunk<T> implements IOutputEmbeddingChunk<T>, Serializable {

	private UUID id;
	private T embedding;

	public IOutputDocument<T> createPartial() {
		Document<T> partial = new Document<>();
		partial.setId(id);
		partial.addEmbedding(embedding);
		return partial;
	}

}
