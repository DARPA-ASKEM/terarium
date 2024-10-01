package software.uncharted.terarium.hmiserver.utils;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Iterator;

public class JsonToMarkdown {

	// Function to recursively render JsonNode object into Markdown
	public static String renderJsonToMarkdown(JsonNode jsonNode) {
		StringBuilder markdown = new StringBuilder();
		renderObject(jsonNode, markdown, 0);
		return markdown.toString();
	}

	// Function to render a JsonNode recursively based on its type
	private static void renderObject(JsonNode jsonNode, StringBuilder markdown, int level) {
		// Iterate over fields if it's an ObjectNode
		if (jsonNode.isObject()) {
			Iterator<String> fieldNames = jsonNode.fieldNames();
			while (fieldNames.hasNext()) {
				String fieldName = fieldNames.next();
				JsonNode valueNode = jsonNode.get(fieldName);

				// Render section headers dynamically based on depth (e.g., #, ##, ###)
				markdown.append("#".repeat(level + 1)).append(" ").append(capitalizeFirstLetter(fieldName)).append("\n");

				// Process the value based on its type
				if (valueNode.isObject()) {
					renderObject(valueNode, markdown, level + 1); // Recurse for nested objects
				} else if (valueNode.isArray()) {
					renderArray(valueNode, markdown, level + 1); // Handle arrays
				} else {
					markdown.append(valueNode.asText()).append("\n\n"); // Render scalar values (e.g., strings, numbers)
				}
			}
		}
	}

	// Function to render an array
	private static void renderArray(JsonNode arrayNode, StringBuilder markdown, int level) {
		for (JsonNode itemNode : arrayNode) {
			// If the item is an object, recurse
			if (itemNode.isObject()) {
				renderObject(itemNode, markdown, level);
			} else {
				markdown.append("- ").append(itemNode.asText()).append("\n"); // Render array items as bullet points
			}
		}
		markdown.append("\n");
	}

	// Helper to capitalize the first letter of a string
	private static String capitalizeFirstLetter(String input) {
		if (input == null || input.isEmpty()) {
			return input;
		}
		return input.substring(0, 1).toUpperCase() + input.substring(1);
	}
}
