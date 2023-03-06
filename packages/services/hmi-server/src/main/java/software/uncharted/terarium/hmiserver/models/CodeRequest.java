package software.uncharted.terarium.hmiserver.models;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;


import java.io.Serializable;
import java.util.List;

/**
 * The CodeRequest instance to send to TA1 for model extraction from text
 */
@NoArgsConstructor
@Data
@Accessors(chain = true)
public class CodeRequest implements Serializable {
	private List<String> files = List.of("test");
	private List<String> blobs;
	@JsonAlias("system_name")
	private String systemName = "";
	@JsonAlias("root_name")
	private String rootName = "";

	public CodeRequest(final String code) {
		blobs = List.of(code);
	}
}
