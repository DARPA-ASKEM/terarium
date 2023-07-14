package software.uncharted.terarium.hmiserver.models.dataservice.modelparts.semantics;

import lombok.Data;
import lombok.experimental.Accessors;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.io.Serializable;

@Data
@Accessors(chain = true)
public class TypingSemantics implements Serializable {
	@JsonAlias("type_map")
	private List<List<String>> map;
	@JsonAlias("type_system")
	private TypeSystem system;
}
