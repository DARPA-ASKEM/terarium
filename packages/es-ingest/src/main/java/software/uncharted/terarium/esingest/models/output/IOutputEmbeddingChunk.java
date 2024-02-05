package software.uncharted.terarium.esingest.models.output;

import java.util.UUID;

public interface IOutputEmbeddingChunk {

	UUID getId();

	Embedding getEmbedding();

	IOutputDocument createPartial();

}
