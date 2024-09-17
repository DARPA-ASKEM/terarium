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
public class EquationVariable extends SupportAdditionalProperties implements Serializable {

	@Serial
	private static final long serialVersionUID = 7329915432714371645L;

	private String id;

	private String text;

	private String image;

	@Override
	public EquationVariable clone() {
		EquationVariable clone = (EquationVariable) super.clone();
		clone.id = this.id;
		clone.text = this.text;
		clone.image = this.image;
		return clone;
	}
}
