package software.uncharted.terarium.hmiserver.models.climateData;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import java.util.UUID;
import lombok.Data;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;

@Data
@Entity
public class ClimateDataPreview {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@TSOptional
	@Schema(accessMode = Schema.AccessMode.READ_ONLY)
	private UUID id;

	private String esgfId;
	private String variableId;
	private String timestamps;
	private String timeIndex;
	private String pngUrl;

	@Column(length = 4096)
	private String error;

	public ClimateDataPreview(ClimateDataPreviewTask previewTask) {
		this.esgfId = previewTask.getEsgfId();
		this.variableId = previewTask.getVariableId();
		this.timestamps = previewTask.getTimestamps();
		this.timeIndex = previewTask.getTimeIndex();
	}

	public ClimateDataPreview(ClimateDataPreviewTask previewTask, String error) {
		this(previewTask);
		this.error = error;
	}

	public ClimateDataPreview(ClimateDataPreviewTask previewTask, JsonNode jobError) {
		this(previewTask, jobError.toString());
	}

	public ClimateDataPreview() {}
}
