package software.uncharted.terarium.hmiserver.proxies.dataservice;

import org.springframework.cloud.openfeign.FeignClient;
import software.uncharted.terarium.hmiserver.models.dataservice.model.ModelConfiguration;


@FeignClient(name = "modelsConfig", url = "${terarium.dataservice.url}", path = "/model_configurations")

public interface ModelConfigurationProxy extends TDSProxy<ModelConfiguration> {

}
