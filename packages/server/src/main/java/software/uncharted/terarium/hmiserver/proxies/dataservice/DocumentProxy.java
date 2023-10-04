package software.uncharted.terarium.hmiserver.proxies.dataservice;

import org.springframework.cloud.openfeign.FeignClient;
import software.uncharted.terarium.hmiserver.models.dataservice.document.DocumentAsset;

@FeignClient(name = "documents", url = "${terarium.dataservice.url}", path = "/documents")
public interface DocumentProxy extends TDSProxy<DocumentAsset> {
}
