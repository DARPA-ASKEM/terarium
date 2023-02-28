package software.uncharted.terarium.hmiserver.models.documentservice.autocomplete;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.json.bind.annotation.JsonbProperty;
import java.io.Serializable;
import java.util.List;

@Data
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Suggest implements Serializable {

	private List<EntitySuggestFuzzy> entitySuggestFuzzy;

	@JsonbProperty("entity-suggest-fuzzy")
	public void setEntitySuggestFuzzy(List<EntitySuggestFuzzy> entitySuggestFuzzy) {
		this.entitySuggestFuzzy = entitySuggestFuzzy;
	}

}
