package software.uncharted.terarium.hmiserver.models.dataservice;

import lombok.Data;
import lombok.experimental.Accessors;
import java.io.Serializable;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;
import software.uncharted.terarium.hmiserver.models.dataservice.Model;

@Data
@Accessors(chain = true)
@TSModel
public class ModelConfiguration {
    private String name;
    @TSOptional
    private String description;
    private String modelId;
    private Model configuration;
}
