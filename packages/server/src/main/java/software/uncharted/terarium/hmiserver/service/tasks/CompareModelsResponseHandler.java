package software.uncharted.terarium.hmiserver.service.tasks;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CompareModelsResponseHandler extends TaskResponseHandler {
    public static final String NAME = "gollm_task:compare_models";

    @Override
    public String getName() {
        return NAME;
    }

    @Data
    public static class Input {
        @JsonProperty("model_cards")
        List<String> modelCards;
    }

    @Data
    public static class Properties {
        List<UUID> modelIds;
    }

    @Data
    public static class Response {
        JsonNode response;
    }
}
