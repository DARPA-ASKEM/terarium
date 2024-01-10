package software.uncharted.terarium.hmiserver.proxies.skema;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import software.uncharted.terarium.hmiserver.models.dataservice.model.Model;

@FeignClient(name = "skema-unified", url = "${skema-unified.url}")
public interface SkemaUnifiedProxy {

	/**
	 * Converts a LaTeX equation to an AMR of different framework via TA1 Skema-unified.
	 *
	 * @param request the Json containing the LaTeX equations and model framework
	 *                https://skema-unified.staging.terarium.ai/docs#/workflows/equations_to_amr_workflows_latex_equations_to_amr_post
	 *                ie: { "equations": [ "equation1", "equation2", ... ], "model": "petrinet" }
	 * @return AMR Model
	 */
	@PostMapping("/workflows/latex/equations-to-amr")
	ResponseEntity<Model> postLaTeXToAMR(@RequestBody JsonNode request);

	@PostMapping("/eqn2mml/image/base64/mml")
	ResponseEntity<String> postImageToEquations(@RequestBody String request);
}
