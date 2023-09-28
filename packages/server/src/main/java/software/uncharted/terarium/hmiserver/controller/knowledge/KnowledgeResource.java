package software.uncharted.terarium.hmiserver.controller.knowledge;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import software.uncharted.terarium.hmiserver.models.extractionservice.ExtractionResponse;
import software.uncharted.terarium.hmiserver.proxies.knowledge.KnowledgeMiddlewareProxy;
import software.uncharted.terarium.hmiserver.proxies.skema.SkemaUnifiedProxy;
import software.uncharted.terarium.hmiserver.proxies.dataservice.ArtifactProxy;

import java.util.List;

@RequestMapping("/knowledge")
@RestController
@Slf4j
public class KnowledgeResource {

	@Autowired
	KnowledgeMiddlewareProxy knowledgeMiddlewareProxy;

	@Autowired
	SkemaUnifiedProxy skemaUnifiedProxy;

	@Autowired
	ArtifactProxy artifactProxy;

	/**
	 * Retrieve the status of an extraction job
	 *
	 * @param id (String) the id of the extraction job
	 * @return the status of the extraction job
	 */
	@GetMapping("/status/{id}")
	public ResponseEntity<JsonNode> getTaskStatus(
		@PathVariable("id") final String id) {
		return ResponseEntity.ok(knowledgeMiddlewareProxy.getTaskStatus(id).getBody());
	}

	/**
	 * Post LaTeX to SKEMA Unified service to get an AMR
	 *
	 * @param framework (String) the type of AMR to return. Options: "regnet", "petrinet".
	 * @param modelId   (String): the id of the model (to update) based on the set of equations
	 * @param equations (List<String>): A list of LaTeX strings representing the functions that are used to convert to AMR model
	 * @return (ExtractionResponse): The response from the extraction service
	 */
	@PostMapping("/mathml-to-amr")
	public ResponseEntity<ExtractionResponse> postMathMLToAMR(
		@RequestParam("framework") String framework,
		@RequestParam("modelId") String modelId,
		@RequestBody List<String> equations
	) {
		return ResponseEntity.ok(knowledgeMiddlewareProxy.postEquationsToAMR("mathml", framework, modelId, equations).getBody());
	};

	/**
	 * Post LaTeX to SKEMA Unified service to get an AMR
	 *
	 * @param framework (String) the type of AMR to return. Options: "regnet", "petrinet".
	 * @param modelId   (String): the id of the model (to update) based on the set of equations
	 * @param equations (List<String>): A list of LaTeX strings representing the functions that are used to convert to AMR model
	 * @return (ExtractionResponse): The response from the extraction service
	 */
	@PostMapping("/latex-to-amr")
	public ResponseEntity<ExtractionResponse> postLaTeXToAMR(
		@RequestParam("framework") String framework,
		@RequestParam ("modelId") String modelId,
		@RequestBody List<String> equations
	) {
		// http://knowledge-middleware.staging.terarium.ai/#/default/equations_to_amr_equations_to_amr_post
		return ResponseEntity.ok(knowledgeMiddlewareProxy.postEquationsToAMR("latex", framework, modelId, equations).getBody());
	};

	/**
	 * Transform source code to AMR
	 * @param 	codeId (String): id of the code artifact
	 * @param 	name (String): the name to set on the newly created model
	 * @param 	description (String): the description to set on the newly created model
	 * @return  (ExtractionResponse)
	 */
	@PostMapping("/code-to-amr")
	ResponseEntity<ExtractionResponse> postCodeToAMR(
		@RequestParam("code_id") String codeId,
		@RequestParam("name") String name,
		@RequestParam("description") String description
	) {
		return ResponseEntity.ok(knowledgeMiddlewareProxy.postCodeToAMR(codeId, name, description).getBody());
	}


	/**
	 * Post a PDF to the extraction service
	 *
	 * @param annotateSkema (Boolean): Whether to annotate the PDF with Skema
	 * @param annotateMIT   (Boolean): Whether to annotate the PDF with AMR
	 * @param name          (String): The name of the PDF
	 * @param description   (String): The description of the PDF
	 *                      <p>
	 *                      Args:
	 *                      pdf (Object): The PDF file to upload
	 * @return response status of queueing this operation
	 */
	@PostMapping("/pdf-extractions")
	public ResponseEntity<JsonNode> postPDFExtractions(
		@RequestParam("artifact_id") String artifactId,
		@RequestParam(name ="annotate_skema", defaultValue = "true") Boolean annotateSkema,
		@RequestParam(name = "annotate_mit", defaultValue = "true") Boolean annotateMIT,
		@RequestParam("name") String name,
		@RequestParam("description") String description
	) {
		return ResponseEntity.ok(knowledgeMiddlewareProxy.postPDFExtractions(artifactId, annotateSkema, annotateMIT, name, description).getBody());
	};

	/**
	 * Post a PDF to the extraction service to get text
	 *
	 * @param artifactId (String): The ID of the artifact to extract text from
	 * @return response status of queueing this operation
	 */
	@PostMapping("/pdf-to-text")
	public ResponseEntity<JsonNode> postPDFToText(@RequestParam("artifact_id") String artifactId){
		return ResponseEntity.ok(knowledgeMiddlewareProxy.postPDFToText(artifactId).getBody());
	}

	/**
	 * Profile a model
	 *
	 * @return the profiled model
	 * @param    modelId (String): The ID of the model to profile
	 * @param    artifactId (String): The text of the document to profile
	 */
	@PostMapping("/profile-model/{model_id}")
	public ResponseEntity<JsonNode> postProfileModel(
		@PathVariable("model_id") String modelId,
		@RequestParam("artifact_id") String artifactId
	) {
		return ResponseEntity.ok(knowledgeMiddlewareProxy.postProfileModel(modelId, artifactId).getBody());
	};

	/**
	 * Profile a dataset
	 *
	 * @return the profiled dataset
	 * @param    datasetId (String): The ID of the dataset to profile
	 * @param    artifactId (String): The ID of the artifact to profile
	 */
	@PostMapping("/profile-dataset/{dataset_id}")
	public ResponseEntity<JsonNode> postProfileDataset(
		@PathVariable("dataset_id") String datasetId,
		@RequestParam("artifact_id") String artifactId
	) {
		return ResponseEntity.ok(knowledgeMiddlewareProxy.postProfileDataset(datasetId, artifactId).getBody());
	};


}
