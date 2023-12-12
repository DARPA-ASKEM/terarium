package software.uncharted.terarium.hmiserver.proxies.dataservice;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import software.uncharted.terarium.hmiserver.models.data.simulation.Simulation;
import software.uncharted.terarium.hmiserver.models.dataservice.dataset.Dataset;


@FeignClient(name = "simulationService", url = "${terarium.dataservice.url}", path = "/simulations")
public interface SimulationProxy extends TDSProxy<Simulation> {

	@GetMapping("/{id}/copy_results")
	ResponseEntity<Dataset> copyResultsToDataset(
		@PathVariable("id") String id
	);
}
