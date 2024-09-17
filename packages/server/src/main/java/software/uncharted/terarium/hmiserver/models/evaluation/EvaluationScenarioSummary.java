package software.uncharted.terarium.hmiserver.models.evaluation;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;

@Data
@Accessors(chain = true)
@NoArgsConstructor
@TSModel
public class EvaluationScenarioSummary {

	private String name;
	private String userId;
	private String task;
	private String description;
	private String notes;
	private Boolean multipleUsers;
	private Long timestampMillis;
}
