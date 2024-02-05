package software.uncharted.terarium.esingest.models.output;

import java.util.UUID;

public interface IOutputDocument {

	void setId(UUID uuid);

	UUID getId();

	void addEmbedding(Embedding embedding);

}
