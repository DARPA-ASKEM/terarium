package software.uncharted.terarium.hmiserver.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class JsonUtil {

	public static <T> void addList(final JsonNode jsonNode, final String key, final List<T> list) {
		if (list != null) {
			final ObjectMapper mapper = new ObjectMapper();
			final ArrayNode arrayNode = mapper.createArrayNode();
			for (final T t : list) {
				final JsonNode e = mapper.valueToTree(t);
				arrayNode.add(e);
			}
			((ObjectNode) jsonNode).set(key, arrayNode);
		}
	}

	public static List<JsonNode> getList(final JsonNode jsonNode, final String key) {
		if (jsonNode == null) {
			return null;
		}
		if (jsonNode.has(key) && jsonNode.get(key).isArray()) {
			final List<JsonNode> list = new ArrayList<>();
			for (final JsonNode node : jsonNode.get(key)) {
				list.add(node);
			}
			return list;
		}
		return null;
	}

	public static void setAll(final ObjectNode dest, final JsonNode src) {
		src.fields().forEachRemaining(entry -> {
			final String fieldName = entry.getKey();
			final JsonNode fieldValue = entry.getValue();
			dest.set(fieldName, fieldValue);
		});
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
}
