package software.uncharted.terarium.hmiserver.models.simulationservice;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Map;
import java.util.List;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import com.fasterxml.jackson.annotation.JsonAlias;

@Data
@Accessors(chain = true)
@TSModel
// Used to specify the location of a dataset for simulation-service
public class EnsembleModelConfigs {
    private String id;
    @JsonAlias("solution_mappings")
    private List<Map<String, String>> solutionMappings;
    private float weight;

}
