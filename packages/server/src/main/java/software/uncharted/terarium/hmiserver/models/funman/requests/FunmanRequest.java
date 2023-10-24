package software.uncharted.terarium.hmiserver.models.funman.requests;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.models.funman.config.FunmanConfig;

@Data
@Accessors(chain = true)
@TSModel
public class FunmanRequest {
    private JsonNode query;
    private List<JsonNode> parameters;
    private FunmanConfig config;
    private List<JsonNode> structureParameters;
}