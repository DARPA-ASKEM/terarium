package software.uncharted.terarium.hmiserver.models.documentservice.responses;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class XDDSetsResponse {

    private String description;

    @JsonProperty("available_sets")
    private List<String> availableSets;
}
