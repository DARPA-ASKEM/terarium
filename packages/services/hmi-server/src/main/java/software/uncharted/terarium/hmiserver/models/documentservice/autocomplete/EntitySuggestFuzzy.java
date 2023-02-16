package software.uncharted.terarium.hmiserver.models.documentservice.autocomplete;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

@Data
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class EntitySuggestFuzzy implements Serializable {

	// We don't care what this is here, we just need to know how many it is, hence Object
	private List<Object> options;
}
