package software.uncharted.terarium.esingest.models.output;

import java.util.UUID;

public interface IOutputDocument<EmbeddingType> {

	void setId(UUID uuid);

	UUID getId();

	void addEmbedding(EmbeddingType embedding);

}
