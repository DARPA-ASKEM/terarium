package software.uncharted.terarium.hmiserver.proxies.simulationservice;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import software.uncharted.terarium.hmiserver.models.simulationservice.JobResponse;

@FeignClient(name = "simulation-service", url = "${simulation-service.url}")
public interface SimulationServiceProxy {
	@PostMapping("/simulate")
	ResponseEntity<JobResponse> makeForecastRun(@RequestBody JsonNode request);

	@PostMapping("/calibrate")
	ResponseEntity<JobResponse> makeCalibrateJob(@RequestParam("queue") String queue, @RequestBody JsonNode request);

	@GetMapping("/runs/{runId}/status")
	ResponseEntity<JsonNode> getRunStatus(@PathVariable("runId") String runId);

	@GetMapping("/runs/{runId}/result")
	ResponseEntity<JsonNode> getRunResult(@PathVariable("runId") String runId);

	@GetMapping("/model-equation/{modelId}")
	ResponseEntity<JsonNode> getModelEquation(@PathVariable("modelId") String modelId);
}
