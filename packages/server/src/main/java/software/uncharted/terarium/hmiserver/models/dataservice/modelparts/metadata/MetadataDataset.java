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
public class MetadataDataset extends SupportAdditionalProperties implements Serializable {

	@Serial
	private static final long serialVersionUID = 1513309568636474748L;

	private String id;

	private String name;

	private String metadata;

	@Override
	public MetadataDataset clone() {
		MetadataDataset clone = (MetadataDataset) super.clone();
		clone.id = this.id;
		clone.name = this.name;
		clone.metadata = this.metadata;
		return clone;
	}
}
