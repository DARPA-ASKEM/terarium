package software.uncharted.terarium.esingest.models.output;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

public interface IOutputDocument {

	@JsonIgnore
	UUID getId();

	void addTopics(List<String> topics);

}
