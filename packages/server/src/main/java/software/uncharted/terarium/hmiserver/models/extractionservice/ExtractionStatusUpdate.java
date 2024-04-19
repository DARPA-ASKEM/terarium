package software.uncharted.terarium.hmiserver.models.extractionservice;

import java.util.UUID;
import lombok.Data;
import software.uncharted.terarium.hmiserver.annotations.TSModel;

@Data
@TSModel
public class ExtractionStatusUpdate {
	private UUID notificationGroupId;
	private UUID documentId;
	private Double t;
	private String message;
	private String error;

	public ExtractionStatusUpdate(final UUID notificationGroupId, final UUID documentId, final Double t, final String message, final String error) {
		this.notificationGroupId = notificationGroupId;
		this.documentId = documentId;
		this.t = t;
		this.message = message;
		this.error = error;
	}
}
