package software.uncharted.terarium.hmiserver.proxies.simulationservice;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import com.fasterxml.jackson.databind.JsonNode;

import software.uncharted.terarium.hmiserver.models.simulationservice.SimulationRequest;
import software.uncharted.terarium.hmiserver.models.simulationservice.JobResponse;

@FeignClient(name = "simulation-service", url = "${terarium.dataservice.url}", path = "/simulation-service")
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
