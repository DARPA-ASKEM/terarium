package software.uncharted.terarium.hmiserver.proxies.modelservice;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import software.uncharted.terarium.hmiserver.models.modelservice.PetriNet;
import software.uncharted.terarium.hmiserver.models.modelservice.StratifyRequest;

@FeignClient(name = "model-service", url = "${model-service.url}", path = "/api")
public interface ModelServiceProxy {
	@PostMapping("/petri-to-latex")
	ResponseEntity<JsonNode> petrinetToLatex(
		@RequestBody PetriNet content
	);

	@PostMapping("/stratify")
	ResponseEntity<JsonNode> stratify(
		@RequestBody StratifyRequest req
	);
}
