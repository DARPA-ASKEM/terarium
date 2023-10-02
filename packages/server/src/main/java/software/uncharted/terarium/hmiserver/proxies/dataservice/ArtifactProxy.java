package software.uncharted.terarium.hmiserver.proxies.dataservice;

import org.springframework.cloud.openfeign.FeignClient;
import software.uncharted.terarium.hmiserver.models.dataservice.Artifact;


/**
 * Proxies TDS artifacts endpoints
 */
@FeignClient(name = "artifacts", url = "${terarium.dataservice.url}", path = "/artifacts")
public interface ArtifactProxy extends TDSProxy<Artifact> {

}
