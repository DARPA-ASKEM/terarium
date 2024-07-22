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
public class DataColumn extends SupportAdditionalProperties implements Serializable {

	@Serial
	private static final long serialVersionUID = 892484869215158123L;

	private String id;

	private String name;

	private MetadataDataset dataset;

	@Override
	public DataColumn clone() {
		DataColumn clone = (DataColumn) super.clone();
		clone.id = id;
		clone.name = name;
		if (dataset != null) clone.dataset = dataset.clone();
		return clone;
	}
}
