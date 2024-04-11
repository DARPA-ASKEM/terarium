package software.uncharted.terarium.hmiserver.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Iterator;
import java.util.Map;

public class JsonUtil {
  public static void recursiveSetAll(final ObjectNode node1, final JsonNode node2) {
    if (node2.isObject()) {
      final Iterator<Map.Entry<String, JsonNode>> fields = node2.fields();
      while (fields.hasNext()) {
        final Map.Entry<String, JsonNode> field = fields.next();
        final String fieldName = field.getKey();
        final JsonNode fieldValue2 = field.getValue();
        if (node1.has(fieldName) && node1.get(fieldName).isObject() && fieldValue2.isObject()) {
          // If the same field is an ObjectNode in both nodes, recursively set all fields
          recursiveSetAll((ObjectNode) node1.get(fieldName), fieldValue2);
        } else {
          // Otherwise, just set the field in node1 to the value in node2
          node1.set(fieldName, fieldValue2);
        }
      }
    }
  }
}
