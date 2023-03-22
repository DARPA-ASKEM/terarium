package software.uncharted.terarium.hmiserver.models.dataservice;

import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.models.petrinet.S;

import java.io.Serializable;
import java.util.*;

@Data
@Accessors(chain = true)
public class ModelContent implements Serializable {
	private List<S> S;
	private List<Map<String, Optional<String>>> T;
	private List<Map<String, Number>> I;
	private List<Map<String, Number>> O;
}
