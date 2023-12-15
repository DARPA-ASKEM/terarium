package software.uncharted.terarium.hmiserver.models.dataservice.model;

import java.io.Serializable;
import java.sql.Timestamp;

import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.ModelHeader;

@Data
@Accessors(chain = true)
@TSModel
public class ModelDescription implements Serializable {

	static public ModelDescription fromModel(Model model) {

		ModelDescription desc = new ModelDescription()
				.setUserId(model.getUserId())
				.setTimestamp(model.getCreatedOn());

		if (model.getHeader() != null) {
			desc.setHeader(new ModelHeader()
					.setName(model.getHeader().getName())
					.setDescription(model.getHeader().getDescription())
					.setModelSchema(model.getHeader().getModelSchema())
					.setSchemaName(model.getHeader().getSchemaName())
					.setModelVersion(model.getHeader().getModelVersion())
					.setExtractedFrom(model.getHeader().getExtractedFrom()));
		}

		return desc;
	}

	private ModelHeader header;
	private Timestamp timestamp;

	@TSOptional
	private String userId;
}
