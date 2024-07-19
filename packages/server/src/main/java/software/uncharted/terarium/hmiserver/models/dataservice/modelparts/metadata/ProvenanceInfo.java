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
public class ProvenanceInfo extends SupportAdditionalProperties implements Serializable {

	@Serial
	private static final long serialVersionUID = 771343267174786121L;

	private String method;

	private String description;

	@Override
	public ProvenanceInfo clone() {
		ProvenanceInfo clone = (ProvenanceInfo) super.clone();
		clone.method = this.method;
		clone.description = this.description;
		return clone;
	}
}
