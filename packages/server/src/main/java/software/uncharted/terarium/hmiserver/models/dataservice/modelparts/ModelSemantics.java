package software.uncharted.terarium.hmiserver.models.dataservice.modelparts;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.AMRSchemaType;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;
import software.uncharted.terarium.hmiserver.models.SupportAdditionalProperties;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.semantics.OdeSemantics;

@Data
@EqualsAndHashCode(callSuper = true)
@AMRSchemaType
@Accessors(chain = true)
public class ModelSemantics extends SupportAdditionalProperties implements Serializable {

	@Serial
	private static final long serialVersionUID = -3980275395523359973L;

	private OdeSemantics ode;

	@TSOptional
	private List<JsonNode> span;

	@TSOptional
	private JsonNode typing;

	@Override
	public ModelSemantics clone() {
		ModelSemantics clone = (ModelSemantics) super.clone();

		if (this.ode != null) {
			clone.ode = this.ode.clone();
		}

		if (this.span != null) {
			clone.span = new ArrayList<>();
			for (JsonNode s : this.span) {
				clone.span.add(s.deepCopy());
			}
		}

		if (this.typing != null) {
			clone.typing = this.typing.deepCopy();
		}

		return clone;
	}
}
