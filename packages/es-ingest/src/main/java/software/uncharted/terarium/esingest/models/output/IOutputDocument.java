package software.uncharted.terarium.esingest.models.output;

import java.util.List;
import java.util.UUID;

public interface IOutputDocument {

	void setId(UUID uuid);

	UUID getId();

	void addTopics(List<String> topics);

	void addEmbedding(Embedding embedding);

}
