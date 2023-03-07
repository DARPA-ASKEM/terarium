package software.uncharted.terarium.hmiserver.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
@Slf4j
public class StructuredLog {
	public enum Type {
		REQUEST_STARTED,
		REQUEST_COMPLETED,
		EVENT
	}

	@ConfigProperty(name = "quarkus.log.console.json", defaultValue = "false")
	Boolean JSON_LOGGING_ENABLED;

	private final ObjectMapper mapper = new ObjectMapper();
	private final String ANONYMOUS_USER = "Anonymous";

	/**
	 * Prints a structure log message for the purpose of capturing in monitoring. If JSON logging is not enabled, it prints
	 * a more user friendly version on a single line separated by the "|" character
	 * @param type						the log type, used a label in grafana
	 * @param user						the current user, may be null if there is no user context
	 * @param keyValuePairs		a list of key value pairs to output, must be an even number of arguments or null
	 */
	public void log(@NonNull final Type type, final String user, Object... keyValuePairs) {
		if (keyValuePairs != null && keyValuePairs.length % 2 != 0) {
			throw new RuntimeException("Structured logs must have an even number of key value pairs");
		}

		if (JSON_LOGGING_ENABLED) {
			final Map<String, Object> data = new HashMap<>();
			data.put("log_message", type);
			data.put("user", user == null ? ANONYMOUS_USER : user);

			if (keyValuePairs != null) {
				for (int i = 0; i < keyValuePairs.length; i += 2) {
					data.put(keyValuePairs[i].toString(), keyValuePairs[i + 1]);
				}
			}
			log.info(asJsonString(data));
		} else {
			final List<String> elements = new ArrayList<>();
			elements.add(type.toString());
			elements.add(user == null ? ANONYMOUS_USER : user);
			if (keyValuePairs != null) {
				for (int i = 0; i < keyValuePairs.length; i += 2) {
					elements.add(keyValuePairs[i + 1].toString());
				}
			}
			log.info(String.join(" | ", elements));
		}
	}

	/**
	 * Converts an object to a json string
	 * @param o	the object
	 * @return	the json stringified representation of the object
	 */
	private String asJsonString(Object o) {
		try {
			return mapper.writeValueAsString(o);
		} catch (JsonProcessingException e) {
			return null;
		}
	}
}
