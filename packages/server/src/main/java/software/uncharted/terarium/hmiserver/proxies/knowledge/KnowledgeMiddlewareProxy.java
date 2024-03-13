package software.uncharted.terarium.hmiserver.proxies.knowledge;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.fasterxml.jackson.databind.JsonNode;

import software.uncharted.terarium.hmiserver.models.extractionservice.ExtractionResponse;

@FeignClient(name = "knowledge-middleware", url = "${knowledge-middleware.url}")
public interface KnowledgeMiddlewareProxy {

	/**
	 * Retrieve the status of an extraction job
	 *
	 * @param id (String) the id of the extraction job
	 * @return the status of the extraction job
	 */
	@GetMapping("/status/{id}")
	ResponseEntity<JsonNode> getTaskStatus(
			@PathVariable("id") String id);

	/**
	 * Profile a model
	 *
	 * @return the profiled model
	 * @param modelId    (String): The ID of the model to profile
	 * @param documentId (String): The text of the document to profile
	 */
	@PostMapping("/profile_model/{model_id}")
	ResponseEntity<JsonNode> postProfileModel(
			@PathVariable("model_id") String modelId,
			@RequestParam("document_id") String documentId);

	/**
	 * Profile a dataset
	 *
	 * @return the profiled dataset
	 * @param datasetId   (String): The ID of the dataset to profile
	 * @param documentId= (String): The text of the document to profile
	 */
	@PostMapping("/profile_dataset/{dataset_id}")
	ResponseEntity<JsonNode> postProfileDataset(
			@PathVariable("dataset_id") String datasetId,
			@RequestParam(name = "document_id", required = false) String documentId);

	/**
	 * Transform source code to AMR
	 *
	 * @param codeId       (String): id of the code artifact
	 * @param name         (String): the name to set on the newly created model
	 * @param description  (String): the description to set on the newly created
	 *                     model
	 * @param dynamicsOnly (Boolean): whether to only run the amr extraction over
	 *                     specified dynamics from the code object in TDS
	 * @param llm_assisted (Boolean): whether amr extraction is llm assisted
	 * @return (ExtractionResponse)
	 */
	@PostMapping("/code_to_amr")
	ResponseEntity<ExtractionResponse> postCodeToAMR(
			@RequestParam("code_id") String codeId,
			@RequestParam(name = "name", required = false) String name,
			@RequestParam(name = "description", required = false) String description,
			@RequestParam(name = "dynamics_only", required = false) Boolean dynamicsOnly,
			@RequestParam(name = "llm_assisted", required = false) Boolean llmAssisted);

}
