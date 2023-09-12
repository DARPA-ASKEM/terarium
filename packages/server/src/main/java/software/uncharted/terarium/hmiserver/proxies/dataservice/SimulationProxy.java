package software.uncharted.terarium.hmiserver.proxies.dataservice;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import software.uncharted.terarium.hmiserver.models.dataservice.Simulation;
import software.uncharted.terarium.hmiserver.models.dataservice.PresignedURL;
import software.uncharted.terarium.hmiserver.models.dataservice.dataset.Dataset;


@FeignClient(name = "simulationService", url = "${terarium.dataservice.url}", path = "/simulations")
public interface SimulationProxy {

	@GetMapping("/{id}")
	ResponseEntity<Simulation> getSimulation(
		@PathVariable("id") String id
	);

	@PostMapping
	ResponseEntity<Simulation> createSimulation(
		@RequestBody JsonNode simulation
	);

	@PatchMapping("/{id}")
	ResponseEntity<Simulation> updateSimulation(
		@PathVariable("id") String id,
		@RequestBody Simulation simulation
	);

	@DeleteMapping("/{id}")
	ResponseEntity<String> deleteSimulation(
		@PathVariable("id") String id
	);

	@GetMapping("/{id}/upload-url")
	ResponseEntity<PresignedURL> getUploadURL(
		@PathVariable("id") String id,
		@RequestParam("filename") String filename
	);

	@GetMapping("/{id}/download-url")
	ResponseEntity<PresignedURL> getDownloadURL(
		@PathVariable("id") String id,
		@RequestParam("filename") String filename
	);

	@GetMapping("/{id}/copy_results")
	ResponseEntity<Dataset> copyResultsToDataset(
		@PathVariable("id") String id
	);
}
