package software.uncharted.terarium.hmiserver.models.documentservice;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class KnownEntitiesCounts {

    @JsonAlias("askem_object")
    private Integer askemObjectCount;

    @JsonAlias("url_extractions")
    private Integer urlExtractionCount;
}
