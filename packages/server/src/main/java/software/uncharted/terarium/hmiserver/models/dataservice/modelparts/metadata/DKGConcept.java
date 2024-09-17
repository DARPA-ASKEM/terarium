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
public class DKGConcept extends SupportAdditionalProperties implements Serializable {

	@Serial
	private static final long serialVersionUID = 8605126700128460881L;

	private String id;

	private String name;

	private Double score;

	@Override
	public DKGConcept clone() {
		DKGConcept clone = (DKGConcept) super.clone();
		clone.setId(id);
		clone.setName(name);
		clone.setScore(score);
		return clone;
	}
}
