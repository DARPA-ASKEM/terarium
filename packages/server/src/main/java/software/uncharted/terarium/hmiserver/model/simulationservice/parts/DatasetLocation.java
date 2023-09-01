package software.uncharted.terarium.hmiserver.model.simulationservice.parts;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;
import com.fasterxml.jackson.annotation.JsonAlias;

@Data
@Accessors(chain = true)
@TSModel
// Used to specify the location of a dataset for simulation-service
public class DatasetLocation {
    private String id;
    private String filename;

    @TSOptional
    private Object mappings;
}
