package software.uncharted.terarium.esingest.models.input.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;
import software.uncharted.terarium.esingest.models.input.IInputDocument;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ModelMetadata implements IInputDocument {

  @Data
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class Author {
    private String given;
    private String family;
    private String name;
  }
  ;

  @Data
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class PublicationMetadata {
    private String title;
    private String doi;
    private String type;
    private List<String> issn;
    private String journal;
    private String publisher;
    private String year;
    private List<Author> author;
  }
  ;

  private String id;

  @JsonProperty("publication_metadata")
  private PublicationMetadata publicationMetadata;
}
