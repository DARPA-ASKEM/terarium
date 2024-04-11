package software.uncharted.terarium.hmiserver.models.documentservice;

import com.fasterxml.jackson.annotation.JsonAlias;
import java.io.Serializable;
import java.util.List;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class XDDUrlExtraction implements Serializable {

  private String url;

  @JsonAlias("resource_title")
  private String resourceTitle;

  @JsonAlias("extracted_from")
  private List<String> extractedFrom;
}
