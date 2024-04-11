package software.uncharted.terarium.esingest.models.input.climate;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import software.uncharted.terarium.esingest.models.input.IInputDocument;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DocumentSource implements IInputDocument {

  @Data
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class Source {

    private String title;
    private String contents;
    private String doi;
  }

  @JsonAlias("_id")
  String id;

  @JsonAlias("_source")
  Source source;
}
