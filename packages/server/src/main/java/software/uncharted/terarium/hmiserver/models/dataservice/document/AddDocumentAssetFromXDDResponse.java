package software.uncharted.terarium.hmiserver.models.dataservice.document;

import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;

@Data
@TSModel
@Accessors(chain = true)
public class AddDocumentAssetFromXDDResponse {

	private String documentAssetId;

    private boolean pdfUploadError;

    private String extractionJobId;

}
