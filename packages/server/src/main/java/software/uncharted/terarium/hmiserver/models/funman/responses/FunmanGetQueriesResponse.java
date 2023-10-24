package software.uncharted.terarium.hmiserver.models.funman.responses;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.models.dataservice.model.Model;
import software.uncharted.terarium.hmiserver.models.funman.requests.FunmanRequest;
import software.uncharted.terarium.hmiserver.models.funman.utils.FunmanProgress;
import software.uncharted.terarium.hmiserver.models.funman.utils.ParameterSpace;

@Data
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@TSModel
public class FunmanGetQueriesResponse {
    private String id;
    private Model model; 
    private FunmanProgress progress;
    private FunmanRequest request;
    private Boolean done;
    private Boolean error;
    private ParameterSpace parameter_space;
}