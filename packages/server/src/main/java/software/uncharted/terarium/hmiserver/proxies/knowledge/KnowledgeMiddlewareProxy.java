package software.uncharted.terarium.hmiserver.proxies.knowledge;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import software.uncharted.terarium.hmiserver.models.extractionservice.ExtractionResponse;

import java.util.List;


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
		@PathVariable("id") String id
	);

	/**
	 * Post MathML to skema service to get AMR return
	 *
	 * @return AMR model
	 * @param    model (String) the id of the model
	 * <p>
	 * Args:
	 * mathMLPayload (List<String>): A list of MathML strings representing the functions that are
	 * used to convert to AMR model (str, optional): AMR model return type.
	 * Defaults to "petrinet". Options: "regnet", "petrinet".
	 */
	@PostMapping("/mathml_to_amr")
	ResponseEntity<JsonNode> postMathMLToAMR(
		@RequestParam(name = "model", defaultValue = "petrinet") String model,
		@RequestBody List<String> mathMLPayload
	);

	/**
	 * Post a PDF
	 *
	 * @param annotateSkema (Boolean): Whether to annotate the PDF with Skema
	 * @param annotateMIT   (Boolean): Whether to annotate the PDF with AMR
	 * @param name          (String): The name of the PDF
	 * @param description   (String): The description of the PDF
	 *                      <p>
	 *                      Args:
	 *                      pdf (Object): The PDF file to upload
	 * @return extractions of the pdf
	 */
	@PostMapping("/variable_extractions")
	ResponseEntity<JsonNode> postPDFExtractions(
		@RequestParam("document_id") String documentId,
		@RequestParam(name = "annotate_skema", defaultValue = "true") Boolean annotateSkema,
		@RequestParam(name = "annotate_mit", defaultValue = "true") Boolean annotateMIT,
		@RequestParam(name = "name", required = false) String name,
		@RequestParam(name = "description", required = false) String description
	);

	/**
	 * Post a PDF to get text
	 *
	 * @param documentId (String): The ID of the document to extract text from
	 * @return
	 */
	@PostMapping("/pdf_extraction")
	ResponseEntity<JsonNode> postPDFToCosmos(
		@RequestParam("document_id") String documentId
	);

	/**
	 * Profile a model
	 *
	 * @return the profiled model
	 * @param    modelId (String): The ID of the model to profile
	 * @param    documentId (String): The text of the document to profile
	 */
	@PostMapping("/profile_model/{model_id}")
	ResponseEntity<JsonNode> postProfileModel(
		@PathVariable("model_id") String modelId,
		@RequestParam("document_id") String documentId
	);

	/**
	 * Profile a dataset
	 *
	 * @return the profiled dataset
	 * @param    datasetId (String): The ID of the dataset to profile
	 * @param    documentId= (String): The text of the document to profile
	 */
	@PostMapping("/profile_dataset/{dataset_id}")
	ResponseEntity<JsonNode> postProfileDataset(
		@PathVariable("dataset_id") String datasetId,
		@RequestParam (name = "document_id", required = false) String documentId
	);

	/**
	 * Transform source code to AMR
	 *
	 * @param codeId       (String): id of the code artifact
	 * @param name         (String): the name to set on the newly created model
	 * @param description  (String): the description to set on the newly created model
	 * @param dynamicsOnly (Boolean): whether to only run the amr extraction over specified dynamics from the code object in TDS
	 * @param llm_assisted (Boolean): whether amr extraction is llm assisted
	 * @return (ExtractionResponse)
	 */
	@PostMapping("/code_to_amr")
	ResponseEntity<ExtractionResponse> postCodeToAMR(
		@RequestParam("code_id") String codeId,
		@RequestParam(name = "name", required = false) String name,
		@RequestParam(name = "description", required = false) String description,
		@RequestParam(name = "dynamics_only", required = false) Boolean dynamicsOnly,
		@RequestParam(name = "llm_assisted", required = false) Boolean llmAssisted
	);

	/**
	 * Transform Equations equations to AMR
	 *
	 * @param equationType (String): [latex, mathml]
	 * @param framework    (String): AMR model return type. Defaults to "petrinet". Options: "regnet", "petrinet".
	 * @param modelId      (String): the id of the model (to update) based on the set of equations
	 * @param equations    (List<String>): the list of LaTeX strings representing the functions that are used to convert to AMR
	 * @return (ExtractionResponse)
	 */
	@PostMapping("/equations_to_amr")
	ResponseEntity<ExtractionResponse> postEquationsToAMR(
		@RequestParam("equation_type") String equationType,
		@RequestParam("model") String framework,
		@RequestParam(name = "model_id", required = false) String modelId,
		@RequestBody List<String> equations
	);

	@PostMapping("link_amr")
	ResponseEntity<JsonNode> postLinkAmr(
		@RequestParam("document_id") String documentId,
		@RequestParam("model_id") String modelId
	);
}
