package software.uncharted.terarium.hmiserver.models.dataservice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.io.IOException;

@Converter(autoApply = true)
public class JsonConverter implements AttributeConverter<JsonNode, String> {

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Override
	public String convertToDatabaseColumn(final JsonNode attribute) {
		try {
			return objectMapper.writeValueAsString(attribute);
		} catch (final JsonProcessingException e) {
			throw new RuntimeException("Error converting JsonNode to JSON", e);
		}
	}

	@Override
	public JsonNode convertToEntityAttribute(final String dbData) {
		try {
			return objectMapper.readValue(dbData, JsonNode.class);
		} catch (final IOException e) {
			throw new RuntimeException("Error converting JSON to JsonNode", e);
		}
	}
}
