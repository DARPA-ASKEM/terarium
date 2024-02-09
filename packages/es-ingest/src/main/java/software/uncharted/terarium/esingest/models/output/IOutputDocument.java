package software.uncharted.terarium.esingest.models.output;

import java.util.List;

public interface IOutputDocument {

	String getId();

	void addTopics(List<String> topics);

}
