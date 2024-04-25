../packages/server/src/main/java/software/uncharted/terarium/hmiserver/service/tasks/ValidateModelConfigHandler.javapackage software.uncharted.terarium.hmiserver.service.tasks;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import java.util.Optional;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import software.uncharted.terarium.hmiserver.models.task.TaskResponse;
import software.uncharted.terarium.hmiserver.models.dataservice.simulation.Simulation;
import software.uncharted.terarium.hmiserver.models.dataservice.simulation.ProgressState;

import software.uncharted.terarium.hmiserver.service.data.SimulationService;

@Component
@RequiredArgsConstructor
@Slf4j
public class ValidateModelConfigHandler extends TaskResponseHandler {
	public static final String NAME = "funman_task:validate_modelconfig";

	private final ObjectMapper objectMapper;
	private final SimulationService simulationService;

	@Override
	public String getName() {
		return NAME;
	}

	@Data
	public static class Properties {
		UUID simulationId;
	}

	@Override
	public TaskResponse onSuccess(final TaskResponse resp) {
		try {
			final Properties props = resp.getAdditionalProperties(Properties.class);
			final UUID simulationId = props.getSimulationId();

			Optional<Simulation> sim = simulationService.getAsset(simulationId);

			// TODO:
			// - Retrive final result json
			// - Upload final result into S3
			// - Mark simulation as completed, update result file
			final JsonNode result = objectMapper.readValue(resp.getOutput(), JsonNode.class);
			final byte[] bytes = objectMapper.writeValueAsBytes(result.get("response"));
			final HttpEntity fileEntity = new ByteArrayEntity(bytes, ContentType.APPLICATION_OCTET_STREAM);

			simulationService.uploadFile(simulationId, "validation.json", fileEntity, ContentType.TEXT_PLAIN);

			// Mark as done, and set resultFiles
			sim.get().setStatus(ProgressState.COMPLETE);
			simulationService.updateAsset(sim.get());

			System.out.println("");
			System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! " + simulationId.toString());
			System.out.println("");
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
		return resp;
	}
}
