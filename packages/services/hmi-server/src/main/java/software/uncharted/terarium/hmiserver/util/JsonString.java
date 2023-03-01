package software.uncharted.terarium.hmiserver.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonString {
	private static ObjectMapper mapper = new ObjectMapper();

	public static String write(Object o) {
		try {
			return mapper.writeValueAsString(o);
		} catch (JsonProcessingException e) {
			return null;
		}
	}
}
