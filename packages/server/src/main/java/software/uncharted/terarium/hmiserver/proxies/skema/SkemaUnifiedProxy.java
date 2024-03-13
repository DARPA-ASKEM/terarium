package software.uncharted.terarium.hmiserver.proxies.skema;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.fasterxml.jackson.databind.JsonNode;

import software.uncharted.terarium.hmiserver.models.dataservice.model.Model;

@FeignClient(name = "skema-unified", url = "${skema-unified.url}")
public interface SkemaUnifiedProxy {

	@PostMapping("/workflows/latex/equations-to-amr")
	ResponseEntity<Model> postLaTeXToAMR(@RequestBody JsonNode request);

	@PostMapping("/eqn2mml/image/base64/mml")
	ResponseEntity<String> postImageToEquations(@RequestBody String request);

	@PostMapping("/workflows/consolidated/equations-to-amr")
	ResponseEntity<Model> consolidatedEquationsToAMR(@RequestBody JsonNode request);

	@PostMapping("/workflows/images/base64/equations-to-amr")
	ResponseEntity<Model> base64EquationsToAMR(@RequestBody JsonNode request);

	@PostMapping("/workflows/images/base64/equations-to-latex")
	ResponseEntity<String> base64EquationsToLatex(@RequestBody JsonNode request);

}
