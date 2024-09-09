package software.uncharted.terarium.hmiserver.models.dataservice.modelparts.metadata;

import java.io.Serial;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.AMRSchemaType;
import software.uncharted.terarium.hmiserver.models.SupportAdditionalProperties;

@Data
@EqualsAndHashCode(callSuper = true)
@AMRSchemaType
@Accessors(chain = true)
public class VariableMetadata extends SupportAdditionalProperties implements Serializable {

	@Serial
	private static final long serialVersionUID = -7797913621713481462L;

	private String type;

	private String value;

	@Override
	public VariableMetadata clone() {
		final VariableMetadata clone = (VariableMetadata) super.clone();
		clone.value = this.value;
		clone.type = this.type;
		return clone;
	}
}
