package software.uncharted.terarium.hmiserver.utils.hibernate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Converter(autoApply = false)
@Slf4j
public class JpaConverterJson implements AttributeConverter<Object, String> {
	private final static ObjectMapper objectMapper = new ObjectMapper();

	@Override
	public String convertToDatabaseColumn(final Object entity) {
		try {
			return objectMapper.writeValueAsString(entity);
		} catch (final JsonProcessingException e) {
			log.error("Unexpected exception converting object to json string: {}", entity, e);
			return null;
		}
	}

	@Override
	public Object convertToEntityAttribute(final String data) {
		try {
			return objectMapper.readValue(data, Object.class);
		} catch (final IOException e) {
			log.error("Unexpected exception decoding json from database: {}", data, e);
			return null;
		}
	}
}
