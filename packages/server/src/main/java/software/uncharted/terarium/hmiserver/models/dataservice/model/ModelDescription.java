package software.uncharted.terarium.hmiserver.models.dataservice.model;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serial;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.UUID;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;
import software.uncharted.terarium.hmiserver.models.SupportAdditionalProperties;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.ModelHeader;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@TSModel
public class ModelDescription extends SupportAdditionalProperties implements Serializable {

	@Serial
	private static final long serialVersionUID = 7664512323130188442L;

	public static ModelDescription fromModel(final Model model) {
		final ModelDescription desc = new ModelDescription()
			.setUserId(model.getUserId())
			.setTimestamp(model.getCreatedOn());

		if (model.getId() != null) {
			desc.setId(model.getId());
		}

		if (model.getHeader() != null) {
			desc.setHeader(
				new ModelHeader()
					.setName(model.getHeader().getName())
					.setDescription(model.getHeader().getDescription())
					.setModelSchema(model.getHeader().getModelSchema())
					.setSchemaName(model.getHeader().getSchemaName())
					.setModelVersion(model.getHeader().getModelVersion())
					.setExtractedFrom(model.getHeader().getExtractedFrom())
			);
		}
		return desc;
	}

	@Schema(accessMode = Schema.AccessMode.READ_ONLY)
	private UUID id;

	private ModelHeader header;

	private Timestamp timestamp;

	@TSOptional
	private String userId;
}
