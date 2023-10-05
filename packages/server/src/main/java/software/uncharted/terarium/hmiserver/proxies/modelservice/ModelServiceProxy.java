package software.uncharted.terarium.hmiserver.proxies.modelservice;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import software.uncharted.terarium.hmiserver.models.modelservice.PetriNet;
import software.uncharted.terarium.hmiserver.models.modelservice.StratifyRequest;

@FeignClient(name = "model-service", url = "${terarium.dataservice.url}", path = "/model-service")
public interface ModelServiceProxy {
	@PostMapping(value = "/petri-to-latex", produces = {"text/plain", "application/*"})
	ResponseEntity<String> petrinetToLatex(
		@RequestBody PetriNet content
	);

	@PostMapping("/stratify")
	ResponseEntity<JsonNode> stratify(
		@RequestBody StratifyRequest req
	);

}
