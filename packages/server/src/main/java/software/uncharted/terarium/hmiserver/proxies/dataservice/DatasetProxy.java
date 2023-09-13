package software.uncharted.terarium.hmiserver.proxies.dataservice;


import org.springframework.cloud.openfeign.FeignClient;
import software.uncharted.terarium.hmiserver.models.dataservice.dataset.Dataset;

@FeignClient(name = "datasets", url = "${terarium.dataservice.url}", path = "/datasets")
public interface DatasetProxy extends TDSProxy<Dataset>{

}
