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
	JobResponse makeForecastRun(
		JsonNode request
	);

	@PostMapping("/calibrate")
	JobResponse makeCalibrateJob(
		JsonNode request
	);

	@GetMapping("/runs/{runId}/status")
	Response getRunStatus(
		@PathVariable("runId") String runId
	);

	@GetMapping("/runs/{runId}/result")
	Response getRunResult(
		@PathVariable("runId") String runId
	);
}
