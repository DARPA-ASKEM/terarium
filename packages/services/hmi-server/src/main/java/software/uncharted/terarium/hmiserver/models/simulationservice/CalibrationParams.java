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
	private String petri;
	private Map<String, Double> initials;
	private List<Double> t;
	private Map<String, Double> params;
	private Map<String, List<Double>> data;
}
