package software.uncharted.terarium.hmiserver.proxies.simulationservice;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import software.uncharted.terarium.hmiserver.models.simulationservice.JobResponse;

@FeignClient(name = "simulation-service", url = "${simulation-service.url}")
public interface SimulationServiceProxy {
	@PostMapping("/simulate")
	ResponseEntity<JobResponse> makeForecastRun(
		@RequestBody JsonNode request
	);

	@PostMapping("/calibrate")
	ResponseEntity<JobResponse> makeCalibrateJob(
		@RequestBody JsonNode request
	);

	@GetMapping("/runs/{runId}/status")
	ResponseEntity<JsonNode> getRunStatus(
		@PathVariable("runId") String runId
	);

	@GetMapping("/runs/{runId}/result")
	ResponseEntity<JsonNode> getRunResult(
		@PathVariable("runId") String runId
	);
}
