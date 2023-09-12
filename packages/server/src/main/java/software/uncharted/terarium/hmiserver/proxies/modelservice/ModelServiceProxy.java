package software.uncharted.terarium.hmiserver.proxies.modelservice;

import com.fasterxml.jackson.databind.JsonNode;
import software.uncharted.terarium.hmiserver.models.modelservice.StratifyRequest;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import software.uncharted.terarium.hmiserver.models.modelservice.PetriNet;

@FeignClient(name = "model-service", url = "${terarium.dataservice.url}", path = "/model-service")
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
