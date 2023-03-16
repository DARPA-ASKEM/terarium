package software.uncharted.terarium.hmiserver.models.dataservice;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Map;
import java.util.Optional;

@Data
@Accessors(chain = true)
public class ModelContent implements Serializable {
	private Map<String, String>[] S;

	private Map<String, Optional<String>>[] T;

	private Map<String, Number>[] I;

	private Map<String, Number>[] O;
}
