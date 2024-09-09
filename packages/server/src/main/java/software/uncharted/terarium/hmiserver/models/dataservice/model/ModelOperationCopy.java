package software.uncharted.terarium.hmiserver.models.dataservice.model;

import java.io.Serial;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.models.SupportAdditionalProperties;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class ModelOperationCopy extends SupportAdditionalProperties implements Serializable {

	@Serial
	private static final long serialVersionUID = -7385762829927577921L;

	private Long left;

	private String name;

	private String description;

	private String framework;

	private String content;
}
