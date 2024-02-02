package software.uncharted.terarium.esingest.models.output;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class Document<EmbeddingType> implements IOutputDocument<EmbeddingType>, Serializable {

	private UUID id;

	private String title;

	private String fullText;

	private List<EmbeddingType> embeddings;

	public void addEmbedding(EmbeddingType embedding) {
		if (embeddings == null) {
			embeddings = new ArrayList<>();
		}
		embeddings.add(embedding);
	}

}
