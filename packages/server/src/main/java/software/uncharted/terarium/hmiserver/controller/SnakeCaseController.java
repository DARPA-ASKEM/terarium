package software.uncharted.terarium.hmiserver.controller;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;

@Deprecated
public interface SnakeCaseController {
	/**
	 * Serialize a given object to be in snake-case instead of camelCase, as may be required by the proxied endpoint.
	 *
	 * @param object
	 * @return
	 */
	default JsonNode convertObjectToSnakeCaseJsonNode(final Object object) {
		final ObjectMapper mapper = new ObjectMapper();
		mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
		return mapper.convertValue(object, JsonNode.class);
	}
}
