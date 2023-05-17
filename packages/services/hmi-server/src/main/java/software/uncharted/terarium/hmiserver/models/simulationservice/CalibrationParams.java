package software.uncharted.terarium.hmiserver.models.simulationservice;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import software.uncharted.terarium.hmiserver.annotations.TSModel;


@Data
@Accessors(chain = true)
@TSModel
public class CalibrationParams implements Serializable {
	private String model;
	private Map<String, Double> initials;
	private Map<String, Double> params;
	private String timesteps_column;
	private Map<String, String> feature_mappings;
	private String dataset;
}
