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
public class VariableStatementMetadata extends SupportAdditionalProperties implements Serializable {

	@Serial
	private static final long serialVersionUID = 114642246601146629L;

	private String type;

	private String value;

	@Override
	public VariableStatementMetadata clone() {
		VariableStatementMetadata clone = (VariableStatementMetadata) super.clone();
		clone.type = this.type;
		clone.value = this.value;
		return clone;
	}
}
