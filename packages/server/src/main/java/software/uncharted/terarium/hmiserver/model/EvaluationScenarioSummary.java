package software.uncharted.terarium.hmiserver.model;

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
	private String username;
	private String task;
	private String description;
	private String notes;
	private Long timestampMillis;
}
