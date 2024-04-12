package software.uncharted.terarium.hmiserver.models.dataservice.modelparts.metadata;

import java.io.Serial;
import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

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
public class StatementValue extends SupportAdditionalProperties implements Serializable {
	@Serial
	private static final long serialVersionUID = 3800270029449210997L;

	private String value;

	private String type;

	@TSOptional
	@JsonProperty("dkg_grounding")
	private DKGConcept dkgGrounding;
}
