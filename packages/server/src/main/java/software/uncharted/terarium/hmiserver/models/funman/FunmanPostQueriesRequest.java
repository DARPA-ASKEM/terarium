package software.uncharted.terarium.hmiserver.models.funman;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.models.funman.parts.FunmanConfig;
import com.fasterxml.jackson.databind.JsonNode;

@Data
@Accessors(chain = true)
@TSModel
public class FunmanPostQueriesRequest {
    private JsonNode query;
    private List<JsonNode> parameters;
    private FunmanConfig config;
    private List<JsonNode> structureParameters;
}