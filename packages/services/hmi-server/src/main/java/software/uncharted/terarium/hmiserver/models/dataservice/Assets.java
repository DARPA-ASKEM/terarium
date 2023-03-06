package software.uncharted.terarium.hmiserver.models.dataservice;


import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import software.uncharted.terarium.hmiserver.models.documentservice.Extraction;


import java.io.Serializable;
import java.util.*;


@Data
@Accessors(chain = true)
@Slf4j
public class Assets implements Serializable {
	List<Dataset> datasets;
	List<Extraction> extractions;
	List<Intermediate> intermediates;
	List<Model> models;
	List<SimulationPlan> plans;
	List<Publication> publications;
	List<SimulationRun> simulationRuns;
}
