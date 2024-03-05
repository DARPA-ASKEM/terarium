package software.uncharted.terarium.hmiserver.models.task;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@NoArgsConstructor
@Data
public class TaskRequest implements Serializable {
	private UUID id;
	private String script;
	private byte[] input;
	private int timeoutMinutes = 30;

	// Sometimes we have context specific variables what we want to associate with a
	// request but aren't actually used by the task on the other side but are
	// necessary for the response to be processed correctly. This property is used
	// to put those variables in. They aren't used by the taskrunner or the task
	// itself, but are passed through to every response.
	private Object additionalProperties;

	public void setInput(final byte[] bytes) throws JsonProcessingException {
		input = bytes;
	}

	public void setInput(final Object obj) throws JsonProcessingException {
		final ObjectMapper mapper = new ObjectMapper();
		input = mapper.writeValueAsBytes(obj);
	}

	public TaskResponse createResponse(final TaskStatus status) {
		return new TaskResponse()
				.setId(id)
				.setStatus(status)
				.setScript(script)
				.setAdditionalProperties(additionalProperties);
	}

	public <T> T getAdditionalProperties(final Class<T> type) {
		if (type.isInstance(additionalProperties)) {
			return type.cast(additionalProperties);
		} else {
			throw new IllegalArgumentException("Value is not of type " + type.getName());
		}
	}

	@JsonIgnore
	public String getSHA256() {
		try {
			// NOTE: do not include the task id in this hash, we want to determine if the
			// body of the request is unique
			final ObjectMapper mapper = new ObjectMapper();

			final String encodedInput = Base64.getEncoder().encodeToString(input);
			final String encodedAdditionalProperties = Base64.getEncoder()
					.encodeToString(mapper.writeValueAsBytes(additionalProperties));

			final String strHash = String.format("%s-%s-%s", script, encodedInput, encodedAdditionalProperties);
			final MessageDigest md = MessageDigest.getInstance("SHA-256");
			return Base64.getEncoder().encodeToString(md.digest(strHash.getBytes(StandardCharsets.UTF_8)));
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}
}
