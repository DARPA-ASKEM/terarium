package software.uncharted.terarium.hmiserver.models.dataservice;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import java.util.UUID;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;
import software.uncharted.terarium.hmiserver.models.TerariumAsset;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@TSModel
@Entity
public class Summary extends TerariumAsset {

	@Column(columnDefinition = "text")
	@TSOptional
	private String generatedSummary;

	@Column(columnDefinition = "text")
	@TSOptional
	private String humanSummary;

	@TSOptional
	private UUID previousSummary;
}
