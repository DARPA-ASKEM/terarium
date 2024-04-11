package software.uncharted.terarium.esingest.models.output;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import java.util.UUID;

public interface IOutputDocument {

  @JsonIgnore
  UUID getId();

  void addTopics(List<String> topics);
}
