package software.uncharted.terarium.hmiserver.utils;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Iterator;

public class JsonToHTML {

	// Function to recursively render JsonNode object into HTML
	public static String renderJsonToHTML(JsonNode jsonNode) {
		StringBuilder html = new StringBuilder();
		renderObject(jsonNode, html, 2);
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
					.append(level)
					.append(">")
					.append(formatTitle(fieldName))
					.append("</h")
					.append(level)
					.append(">\n");

				if (valueNode.isObject()) {
					renderObject(valueNode, html, level + 2); // Recurse for nested objects
				} else if (valueNode.isArray()) {
					renderArray(valueNode, html, level + 2); // Handle arrays
				} else {
					html.append("<p>").append(valueNode.asText()).append("</p><br>\n");
				}
			}
		}
	}

	// Function to render an array as HTML
	private static void renderArray(JsonNode arrayNode, StringBuilder html, int level) {
		html.append("<ul>\n");

		for (JsonNode itemNode : arrayNode) {
			if (itemNode.isObject()) {
				renderObject(itemNode, html, level + 2);
			} else {
				html.append("<li>").append(itemNode.asText()).append("</li>\n");
			}
		}

		html.append("</ul><br>\n");
	}

	// Helper to capitalize the first letter of a string
	public static String formatTitle(String input) {
		if (input == null || input.isEmpty()) {
			return input;
		}

		// Split the string into words and add a space between each word
		String[] words = input.split("(?=[A-Z])");
		StringBuilder formatted = new StringBuilder();
		for (String word : words) {
			formatted.append(word).append(" ");
		}
		final String title = formatted.toString().trim();
		return title.substring(0, 1).toUpperCase() + title.substring(1);
	}
}
