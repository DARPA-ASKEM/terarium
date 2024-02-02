package software.uncharted.terarium.esingest.models.output;

import java.util.UUID;

public interface IOutputEmbedding<T> {

	UUID getId();

	T getEmbedding();

}
