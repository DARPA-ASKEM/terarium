package software.uncharted.terarium.hmiserver.models.task;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.UUID;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@NoArgsConstructor
@Data
public class TaskRequest implements Serializable {

	public static enum TaskType {
		@JsonAlias("gollm")
		GOLLM("gollm"),
		@JsonAlias("mira")
		MIRA("mira"),
		@JsonAlias("funman")
		FUNMAN("funman"),
		@JsonAlias("equation_extraction")
		EQUATION_EXTRACTION_CPU("equation_extraction"),
		@JsonAlias("equation_extraction_gpu")
		EQUATION_EXTRACTION_GPU("equation_extraction_gpu"),
		@JsonAlias("text_extraction")
		TEXT_EXTRACTION("text_extraction"),
		@JsonAlias("table_extraction")
		TABLE_EXTRACTION("table_extraction");

		private final String value;

		TaskType(final String value) {
			this.value = value;
		}

		public String toString() {
			return value;
		}
	}

	protected TaskType type;
	protected String script;
	protected byte[] input;
	protected int timeoutMinutes = 5;
	protected String userId;
	protected UUID projectId;
	protected boolean noCache = false;

	// Sometimes we have context specific variables what we want to associate with a
	// request but aren't actually used by the task on the other side but are
	// necessary for the response to be processed correctly. This property is used
	// to put those variables in. They aren't used by the taskrunner or the task
	// itself, but are passed through to every response.
	protected Object additionalProperties;

	public void setInput(final byte[] bytes) throws JsonProcessingException {
		input = bytes;
	}

	public void setInput(final Object obj) throws JsonProcessingException {
		final ObjectMapper mapper = new ObjectMapper();
		input = mapper.writeValueAsBytes(obj);
	}

	public <T> T getAdditionalProperties(final Class<T> type) throws JsonProcessingException {
		final ObjectMapper objectMapper = new ObjectMapper();
		return objectMapper.readValue(objectMapper.writeValueAsString(additionalProperties), type);
	}

	@JsonProperty("requestSHA256")
	public String getSHA256() {
		try {
			// NOTE: do not include the task id in this hash, we want to determine if the
			// body of the request is unique
			final ObjectMapper objectMapper = new ObjectMapper();
			// make sure the json serialization is deterministic
			objectMapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);

			final String encodedInput = Base64.getEncoder().encodeToString(input);

			final String strHash = String.format("%s-%s-%s", type, script, encodedInput);
			final MessageDigest md = MessageDigest.getInstance("SHA-256");
			return Base64.getEncoder().encodeToString(md.digest(strHash.getBytes(StandardCharsets.UTF_8)));
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}
}
