package software.uncharted.terarium.hmiserver.models.dataservice.modelparts;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.AMRSchemaType;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;
import software.uncharted.terarium.hmiserver.models.SupportAdditionalProperties;

@Data
@EqualsAndHashCode(callSuper = true)
@AMRSchemaType
@Accessors(chain = true)
public class ModelGrounding extends SupportAdditionalProperties implements Serializable {

	public ModelGrounding() {
		this.identifiers = new HashMap<>();
		this.context = new HashMap<>();
		this.modifiers = JsonNodeFactory.instance.objectNode();
	}

	@Serial
	private static final long serialVersionUID = -4946214209697566543L;

	private Map<String, Object> identifiers;

	@TSOptional
	private Map<String, Object> context;

	@TSOptional
	private Object modifiers;

	@Override
	public ModelGrounding clone() {
		ModelGrounding clone = (ModelGrounding) super.clone();

		// I'm unsure how all of these "Objects" can be cloned?

		if (this.identifiers != null) clone.identifiers = new HashMap<>(this.identifiers);

		if (this.context != null) clone.context = new HashMap<>(this.context);

		clone.modifiers = this.modifiers;

		return clone;
	}
}
