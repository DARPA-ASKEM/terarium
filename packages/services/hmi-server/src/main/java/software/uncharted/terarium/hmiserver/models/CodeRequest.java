package software.uncharted.terarium.hmiserver.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.json.bind.annotation.JsonbProperty;
import java.io.Serializable;
import java.util.List;

@NoArgsConstructor
@Data
@Accessors(chain = true)
public class CodeRequest implements Serializable {
	private List<String> files;
	private List<String> blobs;
	@JsonbProperty("system_name")
	private String systemName;
	@JsonbProperty("root_name")
	private String rootName;
}
