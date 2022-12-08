package software.uncharted.terarium.hmiserver.models.dataservice;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Map;
import java.util.Optional;

@Data
@Accessors(chain = true)
public class ModelContent implements Serializable {
	public Map<String, String>[] S;

	public Map<String, Optional<String>>[] T;

	public Map<String, Number>[] I;

	public Map<String, Number>[] O;
}
