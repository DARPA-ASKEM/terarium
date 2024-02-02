package software.uncharted.terarium.esingest.models.output;

import java.util.UUID;

public interface IOutputEmbeddingChunk<T> {

	UUID getId();

	T getEmbedding();

	IOutputDocument<T> createPartial();

}
