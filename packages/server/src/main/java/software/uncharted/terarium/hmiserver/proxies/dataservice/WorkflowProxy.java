package software.uncharted.terarium.hmiserver.proxies.dataservice;

import org.springframework.cloud.openfeign.FeignClient;
import software.uncharted.terarium.hmiserver.models.dataservice.Workflow;

@FeignClient(name = "workflows", url = "${terarium.dataservice.url}", path = "/workflows")
public interface WorkflowProxy extends TDSProxy<Workflow> {

}
