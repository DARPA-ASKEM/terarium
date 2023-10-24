package software.uncharted.terarium.hmiserver.models.funman.responses;

import software.uncharted.terarium.hmiserver.models.dataservice.model.Model;
import software.uncharted.terarium.hmiserver.models.funman.requests.FunmanRequest;
import software.uncharted.terarium.hmiserver.models.funman.utils.ParameterSpace;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;

@Data
@Accessors(chain = true)
@TSModel
public class FunmanResponse {
    @JsonProperty("parameter_space")
    private ParameterSpace parameterSpace;
    private Model model;
    private FunmanRequest request;

}
