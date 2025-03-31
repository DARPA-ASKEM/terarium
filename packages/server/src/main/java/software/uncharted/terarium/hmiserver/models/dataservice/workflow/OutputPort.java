package software.uncharted.terarium.hmiserver.models.dataservice.workflow;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.UUID;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class OutputPort implements Serializable {

	private UUID id;
	private Long version;
	private String type;
	private String originalType;
	private String status;
	private String label;
	private ArrayNode value;
	private Boolean isOptional;
	private Boolean isSelected;
	private String operatorStatus;
	private JsonNode state;
	private Timestamp timestamp;

	@Override
	public OutputPort clone() {
		final OutputPort clone = new OutputPort();
		clone.setId(id);
		clone.setVersion(version);
		clone.setType(type);
		clone.setOriginalType(originalType);
		clone.setStatus(status);
		clone.setLabel(label);
		if (value != null) {
			clone.setValue(value.deepCopy());
		}
		clone.setIsOptional(isOptional);
		clone.setIsSelected(isSelected);
		clone.setOperatorStatus(operatorStatus);
		if (state != null) {
			clone.setState(state.deepCopy());
		}
		clone.setTimestamp(timestamp);
		return clone;
	}

	// FIXME: backwards compatibility, to be removed
	private Boolean acceptMultiple;
}
