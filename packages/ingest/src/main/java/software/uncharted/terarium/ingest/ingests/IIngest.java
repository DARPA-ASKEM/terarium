package software.uncharted.terarium.ingest.ingests;

import software.uncharted.terarium.ingest.input.IAssetInput;
import software.uncharted.terarium.ingest.input.IEmbeddingInput;

public interface IIngest {

	Class<? extends IAssetInput> getAssetInputClass();

	Class<? extends IEmbeddingInput> getEmbeddingInputClass();

}
