package software.uncharted.terarium.hmiserver.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@NoArgsConstructor
public class StoredModel {
	String id;
	String inputs;
	String outputs;
}
