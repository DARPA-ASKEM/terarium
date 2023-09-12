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


@FeignClient(name = "ciemss-service", url = "${terarium.dataservice.url}", path = "/ciemss-service")
public interface SimulationCiemssServiceProxy {
	@PostMapping("/simulate")
	ResponseEntity<JobResponse> makeForecastRun(
		JsonNode request
	);

	@PostMapping("/calibrate")
	ResponseEntity<JobResponse> makeCalibrateJob(
		JsonNode request
	);

	@PostMapping("/ensemble-simulate")
	ResponseEntity<JobResponse> makeEnsembleSimulateCiemssJob(
		JsonNode request
	);

	@PostMapping("/ensemble-calibrate")
	ResponseEntity<JobResponse> makeEnsembleCalibrateCiemssJob(
		JsonNode request
	);

	@GetMapping("/status/{runId}")
	ResponseEntity<JobResponse> getRunStatus(
		@PathVariable("runId") String runId
	);
}
