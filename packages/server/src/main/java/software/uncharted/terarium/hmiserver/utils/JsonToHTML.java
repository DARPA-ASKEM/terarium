package software.uncharted.terarium.hmiserver.utils;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Iterator;

public class JsonToHTML {

	// Function to recursively render JsonNode object into HTML
	public static String renderJsonToHTML(JsonNode jsonNode) {
		StringBuilder html = new StringBuilder();
		renderObject(jsonNode, html, 0);
		return html.toString();
	}

	// Function to render a JsonNode recursively as HTML based on its type
	private static void renderObject(JsonNode jsonNode, StringBuilder html, int level) {
		if (jsonNode.isObject()) {
			Iterator<String> fieldNames = jsonNode.fieldNames();
			while (fieldNames.hasNext()) {
				String fieldName = fieldNames.next();
				JsonNode valueNode = jsonNode.get(fieldName);

				// Render section headers dynamically based on depth using HTML heading tags (e.g., <h1>, <h2>, <h3>)
				html
					.append("<h")
					.append(level + 1)
					.append(">")
					.append(capitalizeFirstLetter(fieldName))
					.append("</h")
					.append(level + 1)
					.append(">\n");

				if (valueNode.isObject()) {
					renderObject(valueNode, html, level + 1); // Recurse for nested objects
				} else if (valueNode.isArray()) {
					renderArray(valueNode, html, level + 1); // Handle arrays
				} else {
					html.append("<p>").append(valueNode.asText()).append("</p>\n");
				}
			}
		}
	}

	// Function to render an array as HTML
	private static void renderArray(JsonNode arrayNode, StringBuilder html, int level) {
		html.append("<ul>\n");

		for (JsonNode itemNode : arrayNode) {
			if (itemNode.isObject()) {
				renderObject(itemNode, html, level);
			} else {
				html.append("<li>").append(itemNode.asText()).append("</li>\n");
			}
		}

		html.append("</ul>\n");
	}

	// Helper to capitalize the first letter of a string
	private static String capitalizeFirstLetter(String input) {
		if (input == null || input.isEmpty()) {
			return input;
		}
		return input.substring(0, 1).toUpperCase() + input.substring(1);
	}
}
