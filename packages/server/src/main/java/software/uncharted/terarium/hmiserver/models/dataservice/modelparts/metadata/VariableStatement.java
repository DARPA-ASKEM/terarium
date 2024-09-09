package software.uncharted.terarium.hmiserver.models.dataservice.modelparts.metadata;

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

@Data
@EqualsAndHashCode(callSuper = true)
@AMRSchemaType
@Accessors(chain = true)
public class VariableStatement extends SupportAdditionalProperties implements Serializable {

	@Serial
	private static final long serialVersionUID = 7471587288767843423L;

	private String id;

	private Variable variable;

	@TSOptional
	private StatementValue value;

	@TSOptional
	private List<VariableStatementMetadata> metadata;

	@TSOptional
	private ProvenanceInfo provenance;

	@Override
	public VariableStatement clone() {
		VariableStatement clone = (VariableStatement) super.clone();
		clone.id = this.id;
		clone.variable = this.variable.clone();

		if (this.value != null) clone.value = this.value.clone();

		if (this.metadata != null) {
			clone.metadata = new ArrayList<>();
			for (VariableStatementMetadata metadata : this.metadata) clone.metadata.add(metadata.clone());
		}

		if (this.provenance != null) clone.provenance = this.provenance.clone();

		return clone;
	}
}
