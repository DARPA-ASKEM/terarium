package software.uncharted.terarium.hmiserver.proxies.mira;

import feign.FeignException;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import software.uncharted.terarium.hmiserver.models.dataservice.model.Model;
import software.uncharted.terarium.hmiserver.models.mira.Curies;
import software.uncharted.terarium.hmiserver.models.mira.DKG;
import software.uncharted.terarium.hmiserver.models.mira.EntitySimilarityResult;

@FeignClient(name = "mira-api", url = "${mira-api.url}", path = "/api")
public interface MIRAProxy {
	// This converts MIRANet (Petrinet) to AMR
	@PostMapping("/to_petrinet")
	ResponseEntity<Model> toPetrinet(@RequestBody Object obj) throws FeignException;

	@PostMapping("/entity_similarity")
	ResponseEntity<List<EntitySimilarityResult>> entitySimilarity(@RequestBody Curies obj) throws FeignException;

	@PostMapping("/search")
	ResponseEntity<List<DKG>> search(@RequestBody String q, @RequestParam Integer limit, @RequestParam Integer offset)
		throws FeignException;
}
