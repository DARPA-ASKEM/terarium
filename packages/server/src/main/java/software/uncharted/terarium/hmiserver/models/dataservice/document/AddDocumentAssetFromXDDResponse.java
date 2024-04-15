package software.uncharted.terarium.hmiserver.models.dataservice.document;

import java.util.UUID;

import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;

@Data
@TSModel
@Accessors(chain = true)
public class AddDocumentAssetFromXDDResponse {

	private UUID documentAssetId;

	private boolean pdfUploadError;

	private String extractionJobId;

}
