package software.uncharted.terarium.hmiserver.proxies.skema;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "skema-rust", url = "${skema-rs.url}")
public interface SkemaRustProxy {
	/**
	 * Store the model
	 *
	 * @param functionNetwork a JSON string of the function network
	 * @return the model id of the stored model
	 */
	@PostMapping("/models")
	ResponseEntity<String> addModel(String functionNetwork);

	/**
	 * Gets a list of inputs from a stored model id
	 *
	 * @param modelId the id of the stored model
	 * @return the list of inputs
	 */
	@GetMapping("/models/{modelId}/named_opis")
	ResponseEntity<String> getModelNamedOpis(@PathVariable("modelId") String modelId);

	/**
	 * Gets a list of outputs from a stored model id
	 *
	 * @param modelId the id of the stored model
	 * @return the list of outputs
	 */
	@GetMapping("/models/{modelId}/named_opos")
	ResponseEntity<String> getModelNamedOpos(@PathVariable("modelId") String modelId);

	@PutMapping("/mathml/amr")
	ResponseEntity<JsonNode> convertMathML2AMR(@RequestBody JsonNode request);

	@PostMapping("/mathml/latex")
	ResponseEntity<String> convertMathML2Latex(@RequestBody String mathML);
}
