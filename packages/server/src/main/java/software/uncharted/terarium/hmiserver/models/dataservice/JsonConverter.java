package software.uncharted.terarium.hmiserver.models.dataservice;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class JsonConverter implements AttributeConverter<Object, String> {

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Override
	public String convertToDatabaseColumn(final Object attribute) {
		try {
			return objectMapper.writeValueAsString(attribute);
		} catch (final JsonProcessingException e) {
			throw new RuntimeException("Error converting Object to JSON", e);
		}
	}

	@Override
	public Object convertToEntityAttribute(final String dbData) {
		try {
			return objectMapper.readValue(dbData, Object.class);
		} catch (final IOException e) {
			throw new RuntimeException("Error converting JSON to Object", e);
		}
	}
}
