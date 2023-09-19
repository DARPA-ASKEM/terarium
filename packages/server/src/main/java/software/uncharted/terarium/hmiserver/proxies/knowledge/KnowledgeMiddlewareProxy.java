
package software.uncharted.terarium.hmiserver.proxies.knowledge;

import com.fasterxml.jackson.databind.JsonNode;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import software.uncharted.terarium.hmiserver.models.extractionservice.ExtractionResponse;

import java.util.List;



@FeignClient(name = "knowledge-middleware", url = "${knowledge-middleware}")
public interface KnowledgeMiddlewareProxy {

	/**
	 * Retrieve the status of an extraction job
	 * @param id (String) the id of the extraction job
	 * @return the status of the extraction job
	 */
	@GetMapping("/status/{id}")
	ResponseEntity<JsonNode> getTaskStatus(
		@PathVariable("id") String id
	);

	/**
	 * Post MathML to skema service to get AMR return
	 *
	 * @param		model (String) the id of the model
	 *
	 * Args:
	 *     mathMLPayload (List<String>): A list of MathML strings representing the functions that are
	 * 													         used to convert to AMR model (str, optional): AMR model return type.
	 * 													         Defaults to "petrinet". Options: "regnet", "petrinet".
	 *
	 * @return AMR model
	 */
	@PostMapping("/mathml_to_amr")
	ResponseEntity<JsonNode> postMathMLToAMR(
		@RequestParam(name = "model", defaultValue = "petrinet") String model,
		@RequestBody List<String> mathMLPayload
	);

	/**
	 * Post a PDF
	 *
	 * @param    annotateSkema (Boolean): Whether to annotate the PDF with Skema
	 * @param    annotateMIT (Boolean): Whether to annotate the PDF with AMR
	 * @param    name (String): The name of the PDF
	 * @param    description (String): The description of the PDF
	 *
	 * Args:
	 *     pdf (Object): The PDF file to upload
	 *
	 * @return extractions of the pdf
	 */
	@PostMapping("/pdf_extractions")
	ResponseEntity<JsonNode> postPDFExtractions(
		@RequestParam("artifact_id") String artifactId,
		@RequestParam(name = "annotate_skema", defaultValue = "true") Boolean annotateSkema,
		@RequestParam(name = "annotate_mit", defaultValue = "true") Boolean annotateMIT,
		@RequestParam("name") String name,
		@RequestParam("description") String description
	);

	/**
	 * Post a PDF to get text
	 * @param artifactId (String): The ID of the artifact to extract text from
	 * @return
	 */
	@PostMapping("/pdf_to_text")
	ResponseEntity<JsonNode> postPDFToText(
		@RequestParam("artifact_id") String artifactId
	);

	/**
	 * Profile a model
	 *
	 * @param		modelId (String): The ID of the model to profile
	 * @param		artifactId (String): The text of the document to profile
	 *
	 * @return the profiled model
	 */
	@PostMapping("/profile_model/{model_id}")
	ResponseEntity<JsonNode> postProfileModel(
		@PathVariable("model_id") String modelId,
		@RequestParam("paper_artifact_id") String artifactId
	);

	/**
	 * Profile a dataset
	 *
	 * @param		datasetId (String): The ID of the dataset to profile
	 * @param		artifactId (String): The text of the document to profile
	 *
	 * @return the profiled dataset
	 */
	@PostMapping("/profile_dataset/{dataset_id}")
	ResponseEntity<JsonNode> postProfileDataset(
		@PathVariable("dataset_id") String datasetId,
		@RequestParam("artifact_id") String artifactId
	);

	/**
	 * Transform source code to AMR
	 * @param 	codeId (String): id of the code artifact
	 * @param 	name (String): the name to set on the newly created model
	 * @param 	description (String): the description to set on the newly created model
	 * @return  (ExtractionResponse)
	 */
	@PostMapping("/code_to_amr")
	ResponseEntity<ExtractionResponse> postCodeToAMR(
		@RequestParam("code_id") String codeId,
		@RequestParam("name") String name,
		@RequestParam("description") String description
	);

	/**
	 * Transform LaTeX equations to AMR
	 * @param 	equationType (String): [latex, mathml]
	 * @param 	framework (String): AMR model return type. Defaults to "petrinet". Options: "regnet", "petrinet".
	 * @param 	modelId (String): the id of the model (to update) based on the set of equations
	 * @param 	equations (List<String>): the list of LaTeX strings representing the functions that are used to convert to AMR
	 * @return  (ExtractionResponse)
	 */
	@PostMapping("/equations_to_amr")
	ResponseEntity<ExtractionResponse> postLaTeXToAMR(
		@RequestParam("equation_type") String equationType,
		@RequestParam("model") String framework,
		@RequestParam("model_id") String modelId,
		@RequestBody List<String> equations
	);
}
