package software.uncharted.terarium.hmiserver.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JsonUtil {

	public static void setAll(final ObjectNode dest, final JsonNode src) {
		src
			.fields()
			.forEachRemaining(entry -> {
				final String fieldName = entry.getKey();
				final JsonNode fieldValue = entry.getValue();
				dest.set(fieldName, fieldValue);
			});
	}

	public static JsonNode getFirstByPredicate(final ArrayNode node, final Predicate<JsonNode> predicate) {
		for (final JsonNode element : node) {
			if (predicate.test(element)) {
				return element;
			}
		}
		return null; // Return null if no element matches the predicate
	}

	public static void recursiveSetAll(final ObjectNode dest, final JsonNode src) {
		if (src.isObject()) {
			final Iterator<Map.Entry<String, JsonNode>> fields = src.fields();
			while (fields.hasNext()) {
				final Map.Entry<String, JsonNode> field = fields.next();
				final String fieldName = field.getKey();
				final JsonNode fieldValue2 = field.getValue();
				if (dest.has(fieldName) && dest.get(fieldName).isObject() && fieldValue2.isObject()) {
					// If the same field is an ObjectNode in both nodes, recursively set all fields
					recursiveSetAll((ObjectNode) dest.get(fieldName), fieldValue2);
				} else {
					// Otherwise, just set the field in dest to the value in src
					dest.set(fieldName, fieldValue2);
				}
			}
		}
	}

	/**
	 * Parse a UUID from a JSON request
	 * @param jsonNode - The JSON request
	 * @param fieldName - The field name to parse the UUID from
	 * @return - The UUID parsed from the request
	 */
	public static UUID parseUuidFromRequest(JsonNode jsonNode, String fieldName) {
		final String idString = jsonNode.get(fieldName) != null ? jsonNode.get(fieldName).asText() : null;
		if (idString != null) {
			return UUID.fromString(idString);
		}
		return null;
	}
}
