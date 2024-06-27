package software.uncharted.terarium.ingest.input;

import java.util.UUID;

import software.uncharted.terarium.ingest.models.TerariumAssetEmbeddings;

public interface IEmbeddingInput {

	UUID getId();

	TerariumAssetEmbeddings getEmbedding();
}
