package software.uncharted.terarium.hmiserver.proxies.mira;

import feign.FeignException;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import software.uncharted.terarium.hmiserver.models.mira.Curies;
import software.uncharted.terarium.hmiserver.models.mira.EntitySimilarityResult;

@FeignClient(name = "mira-api", url = "${mira-rest-url}", path = "/api")
public interface MIRAProxy {
	@PostMapping("/entity_similarity")
	ResponseEntity<List<EntitySimilarityResult>> entitySimilarity(@RequestBody Curies obj) throws FeignException;
}
