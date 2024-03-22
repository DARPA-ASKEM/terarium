package software.uncharted.terarium.hmiserver.models.dataservice.document;

import java.util.UUID;

import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.models.documentservice.Document;

@Data
@TSModel
@Accessors(chain = true)
public class AddDocumentAssetFromXDDRequest {

	private Document document;

	private UUID projectId;

	private String domain = "epi"; // default value
}
