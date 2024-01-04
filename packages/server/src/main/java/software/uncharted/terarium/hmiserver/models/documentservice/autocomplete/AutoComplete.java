package software.uncharted.terarium.hmiserver.models.documentservice.autocomplete;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Data
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AutoComplete implements Serializable {

	private Suggest suggest;

	private static final String AUTOCOMPLETE_FIELD = "text";

	/**
	 * Looks at the underlying objects returned by XDD and determines if there are any auto complete suggestions.
	 * This is a convenience method meant to make returning error codes cleaner.
	 *
	 * @return true if there are no suggestions.
	 */
	public boolean hasNoSuggestions() {
		return (suggest == null
			|| suggest.getEntitySuggestFuzzy() == null
			|| suggest.getEntitySuggestFuzzy().isEmpty()
			|| suggest.getEntitySuggestFuzzy().get(0).getOptions() == null
			|| suggest.getEntitySuggestFuzzy().get(0).getOptions().isEmpty());
	}

	/**
	 * Returns a list of autocomplete suggestions, or an empty list if there is none
	 *
	 * @return
	 */
	public List<String> getAutoCompletes() {
		List<String> autoCompletes = new ArrayList<>();
		for (Map<String, Object> options : getSuggest().getEntitySuggestFuzzy().get(0).getOptions()) {
			autoCompletes.add(options.get(AUTOCOMPLETE_FIELD).toString());
		}

		return autoCompletes;
	}

}
