package software.uncharted.terarium.hmiserver.models.dataservice.modelparts;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serial;
import java.io.Serializable;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.AMRSchemaType;
import software.uncharted.terarium.hmiserver.models.SupportAdditionalProperties;

@Data
@EqualsAndHashCode(callSuper = true)
@AMRSchemaType
@Accessors(chain = true)
public class ModelDistribution extends SupportAdditionalProperties implements Serializable {

	@Serial
	private static final long serialVersionUID = -5426742497090710018L;

	private String type;

	//TODO the fact that this is `Object` is causing issues, however, I'm not sure if we can make this not an object??
	private Map<String, Object> parameters;

	@Override
	public ModelDistribution clone() {
		final ModelDistribution clone = (ModelDistribution) super.clone();
		clone.setParameters(this.getParameters());
		clone.setType(this.getType());
		return clone;
	}

	@JsonIgnore
	public boolean isMinimumEqualToMaximum() {
		if (parameters == null) return false;
		Object min = parameters.get("minimum");
		Object max = parameters.get("maximum");
		if (min instanceof Number && max instanceof Number) {
			return ((Number) min).doubleValue() == ((Number) max).doubleValue();
		}
		return false;
	}
}
