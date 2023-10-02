package software.uncharted.terarium.hmiserver.proxies.dataservice;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import software.uncharted.terarium.hmiserver.models.dataservice.dataset.Dataset;

@FeignClient(name = "datasets", url = "${terarium.dataservice.url}", path = "/datasets")
public interface DatasetProxy extends TDSProxy<Dataset> {


	@GetMapping("/{id}")
	Dataset getDataset(
		@PathVariable("id") String id
	);

}
