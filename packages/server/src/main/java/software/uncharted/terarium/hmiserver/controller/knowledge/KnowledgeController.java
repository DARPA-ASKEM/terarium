package software.uncharted.terarium.hmiserver.controller.knowledge;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.extern.slf4j.Slf4j;
import software.uncharted.terarium.hmiserver.models.dataservice.provenance.Provenance;
import software.uncharted.terarium.hmiserver.models.dataservice.provenance.ProvenanceRelationType;
import software.uncharted.terarium.hmiserver.models.dataservice.provenance.ProvenanceType;
import software.uncharted.terarium.hmiserver.models.extractionservice.ExtractionResponse;
import software.uncharted.terarium.hmiserver.proxies.knowledge.KnowledgeMiddlewareProxy;
import software.uncharted.terarium.hmiserver.proxies.skema.SkemaUnifiedProxy;
import software.uncharted.terarium.hmiserver.security.Roles;
import software.uncharted.terarium.hmiserver.service.data.ProvenanceService;

@RequestMapping("/knowledge")
@RestController
@Slf4j
@RequiredArgsConstructor
public class KnowledgeController {


	final KnowledgeMiddlewareProxy knowledgeMiddlewareProxy;


	final SkemaUnifiedProxy skemaUnifiedProxy;


	final ProvenanceService provenanceService;

	/**
	 * Retrieve the status of an extraction job
	 *
	 * @param id (String) the id of the extraction job
	 * @return the status of the extraction job
	 */
	@GetMapping("/status/{id}")
	@Secured(Roles.USER)
	public ResponseEntity<JsonNode> getTaskStatus(
			@PathVariable("id") final String id) {
		return ResponseEntity.ok(knowledgeMiddlewareProxy.getTaskStatus(id).getBody());
	}

	/**
	 * Post Equations to SKEMA Unified service to get an AMR
	 *
	 * @param requestMap (Map<String, Object>) JSON request body containing the
	 *                   following fields:
	 *                   - format (String) the format of the equations. Options:
	 *                   "latex", "mathml".
	 *                   - framework (String) the type of AMR to return. Options:
	 *                   "regnet", "petrinet".
	 *                   - modelId (String): the id of the model (to update) based
	 *                   on the set of equations
	 *                   - equations (List<String>): A list of LaTeX strings
	 *                   representing the functions that are used to convert to AMR
	 *                   model
	 * @return (ExtractionResponse): The response from the extraction service
	 */
	@PostMapping("/equations-to-model")
	@Secured(Roles.USER)
	public ResponseEntity<ExtractionResponse> postLaTeXToAMR(@RequestBody Map<String, Object> requestMap) {
		String format = (String) requestMap.getOrDefault("format", "latex");
		String framework = (String) requestMap.getOrDefault("framework", "petrinet");
		String modelId = (String) requestMap.get("modelId");
		List<String> equations = (List<String>) requestMap.getOrDefault("equations", Collections.emptyList());

		// http://knowledge-middleware.staging.terarium.ai/#/default/equations_to_amr_equations_to_amr_post
		return ResponseEntity
				.ok(knowledgeMiddlewareProxy.postEquationsToAMR(format, framework, modelId, equations).getBody());
	}

	/**
	 * Transform source code to AMR
	 *
	 * @param codeId       (String): id of the code artifact
	 * @param name         (String): the name to set on the newly created model
	 * @param description  (String): the description to set on the newly created
	 *                     model
	 * @param dynamicsOnly (Boolean): whether to only run the amr extraction over
	 *                     specified dynamics from the code object in TDS
	 * @param llmAssisted (Boolean): whether amr extraction is llm assisted
	 * @return (ExtractionResponse)
	 */
	@PostMapping("/code-to-amr")
	@Secured(Roles.USER)
	ResponseEntity<ExtractionResponse> postCodeToAMR(
			@RequestParam("code_id") String codeId,
			@RequestParam(name = "name", required = false) String name,
			@RequestParam(name = "description", required = false) String description,
			@RequestParam(name = "dynamics_only", required = false) Boolean dynamicsOnly,
			@RequestParam(name = "llm_assisted", required = false) Boolean llmAssisted) {
		return ResponseEntity.ok(
				knowledgeMiddlewareProxy.postCodeToAMR(codeId, name, description, dynamicsOnly, llmAssisted).getBody());
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
	@Secured(Roles.USER)
	public ResponseEntity<JsonNode> postPDFExtractions(
			@RequestParam("document_id") String documentId,
			@RequestParam(name = "annotate_skema", defaultValue = "true") Boolean annotateSkema,
			@RequestParam(name = "annotate_mit", defaultValue = "true") Boolean annotateMIT,
			@RequestParam(name = "name", required = false) String name,
			@RequestParam(name = "description", required = false) String description) {
		return ResponseEntity.ok(knowledgeMiddlewareProxy
				.postPDFExtractions(documentId, annotateSkema, annotateMIT, name, description).getBody());
	}

	/**
	 * Post a PDF to the extraction service to get text
	 *
	 * @param documentId (String): The ID of the document to extract text from
	 * @return response status of queueing this operation
	 */
	@PostMapping("/pdf-to-cosmos")
	@Secured(Roles.USER)
	public ResponseEntity<JsonNode> postPDFToCosmos(@RequestParam("document_id") String documentId) {

		try {
			JsonNode node = knowledgeMiddlewareProxy.postPDFToCosmos(documentId).getBody();


			return ResponseEntity.ok(node);
		} catch (Exception e) {
			final String error = "Unable to create provenance";
			log.error(error, e);
			throw new ResponseStatusException(
					org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
					error);
		}
	}

	/**
	 * Profile a model
	 *
	 * @param modelId    (String): The ID of the model to profile
	 * @param documentId (String): The text of the document to profile
	 * @return the profiled model
	 */
	@PostMapping("/profile-model/{model_id}")
	@Secured(Roles.USER)
	public ResponseEntity<JsonNode> postProfileModel(
			@PathVariable("model_id") UUID modelId,
			@RequestParam("document_id") UUID documentId) {

		Provenance provenancePayload = new Provenance(ProvenanceRelationType.EXTRACTED_FROM, modelId,
				ProvenanceType.MODEL, documentId, ProvenanceType.DOCUMENT);
		try {
			provenanceService.createProvenance(provenancePayload);
			return ResponseEntity
					.ok(knowledgeMiddlewareProxy.postProfileModel(modelId.toString(), documentId.toString()).getBody());
		} catch (Exception e) {
			final String error = "Unable to create provenance";
			log.error(error, e);
			throw new ResponseStatusException(
					org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
					error);
		}
	}

	;

	/**
	 * Profile a dataset
	 *
	 * @param datasetId   (String): The ID of the dataset to profile
	 * @param documentId= (String): The ID of the document to profile
	 * @return the profiled dataset
	 */
	@PostMapping("/profile-dataset/{dataset_id}")
	@Secured(Roles.USER)
	public ResponseEntity<JsonNode> postProfileDataset(
			@PathVariable("dataset_id") UUID datasetId,
			@RequestParam(name = "document_id", required = false) UUID documentId) {

		Provenance provenancePayload = new Provenance(ProvenanceRelationType.EXTRACTED_FROM, datasetId,
				ProvenanceType.DATASET, documentId, ProvenanceType.DOCUMENT);

		try {
			provenanceService.createProvenance(provenancePayload);
			return ResponseEntity.ok(
					knowledgeMiddlewareProxy.postProfileDataset(datasetId.toString(), documentId.toString()).getBody());
		} catch (Exception e) {
			final String error = "Unable to create provenance";
			log.error(error, e);
			throw new ResponseStatusException(
					org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
					error);
		}
	}

	;

	/**
	 * Profile a model
	 *
	 * @param modelId    (String): The ID of the model to profile
	 * @param documentId (String): The text of the document to profile
	 * @return the profiled model
	 */
	@PostMapping("/link-amr")
	@Secured(Roles.USER)
	public ResponseEntity<JsonNode> postLinkAmr(
			@RequestParam("document_id") String documentId,
			@RequestParam("model_id") String modelId) {
		return ResponseEntity.ok(knowledgeMiddlewareProxy.postLinkAmr(documentId, modelId).getBody());
	}

	;

}
