package software.uncharted.terarium.hmiserver.controller.knowledge;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import software.uncharted.terarium.hmiserver.models.dataservice.PresignedURL;
import software.uncharted.terarium.hmiserver.models.dataservice.code.Code;
import software.uncharted.terarium.hmiserver.models.dataservice.model.Model;
import software.uncharted.terarium.hmiserver.models.dataservice.provenance.Provenance;
import software.uncharted.terarium.hmiserver.models.dataservice.provenance.ProvenanceRelationType;
import software.uncharted.terarium.hmiserver.models.dataservice.provenance.ProvenanceType;
import software.uncharted.terarium.hmiserver.models.extractionservice.ExtractionResponse;
import software.uncharted.terarium.hmiserver.proxies.documentservice.ExtractionProxy;
import software.uncharted.terarium.hmiserver.proxies.knowledge.KnowledgeMiddlewareProxy;
import software.uncharted.terarium.hmiserver.proxies.skema.SkemaUnifiedProxy;
import software.uncharted.terarium.hmiserver.security.Roles;
import software.uncharted.terarium.hmiserver.service.data.CodeService;
import software.uncharted.terarium.hmiserver.service.data.ModelService;
import software.uncharted.terarium.hmiserver.service.data.ProvenanceService;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@RequestMapping("/knowledge")
@RestController
@Slf4j
@RequiredArgsConstructor
public class KnowledgeController {

	final ObjectMapper mapper;

	final KnowledgeMiddlewareProxy knowledgeMiddlewareProxy;

	final SkemaUnifiedProxy skemaUnifiedProxy;

	final ProvenanceService provenanceService;

	final ModelService modelService;

	final CodeService codeService;

	final ExtractionProxy extractionProxy;

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
	 * Send the equations to the skema unified service to get the AMR
	 * @param req
	 * @return
	 */
	@PostMapping("/equations-to-model")
	@Secured(Roles.USER)
	public ResponseEntity<UUID> equationsToModel(@RequestBody final JsonNode req) {
		try {
			final Model responseAMR = skemaUnifiedProxy
						.consolidatedEquationsToAMR(req)
						.getBody();

			if (responseAMR == null) {
				throw new ResponseStatusException(
					HttpStatus.NO_CONTENT,
					"No AMR returned by Skema Unified Service.");
			}

			final UUID modelId = req.get("modelId") != null ? UUID.fromString(req.get("modelId").asText()) : null;
			if (modelId != null) {
				final Optional<Model> model = modelService.getAsset(modelId);
				if (model.isPresent()) {
					responseAMR.setId(model.get().getId());
					modelService.updateAsset(responseAMR);
					return ResponseEntity.ok(model.get().getId());
				} else {
					throw new ResponseStatusException(
						HttpStatus.BAD_REQUEST,
						"The model id provided does not exist.");
				}
			}

			final Model model = modelService.createAsset(responseAMR);
			return ResponseEntity.ok(model.getId());

		} catch (final Exception e) {
			log.error("unable to create model", e);
			throw new ResponseStatusException(
					org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
					"Error creating model");
		}
	}

	@PostMapping("/base64-equations-to-model")
	@Secured(Roles.USER)
	public ResponseEntity<Model> base64EquationsToAMR(@RequestBody final JsonNode req) {
		return ResponseEntity
				.ok(skemaUnifiedProxy
						.base64EquationsToAMR(req)
						.getBody());
	}

	@PostMapping("/base64-equations-to-latex")
	@Secured(Roles.USER)
	public ResponseEntity<String> base64EquationsToLatex(@RequestBody final JsonNode req) {
		return ResponseEntity
				.ok(skemaUnifiedProxy
						.base64EquationsToLatex(req)
						.getBody());
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
	 * @param llmAssisted  (Boolean): whether amr extraction is llm assisted
	 * @return (ExtractionResponse)
	 */
	@PostMapping("/code-to-amr")
	@Secured(Roles.USER)
	ResponseEntity<ExtractionResponse> postCodeToAMR(
			@RequestParam("code_id") final UUID codeId,
			@RequestParam(name = "name", required = false) final String name,
			@RequestParam(name = "description", required = false) final String description,
			@RequestParam(name = "dynamics_only", required = false) final Boolean dynamicsOnly,
			@RequestParam(name = "llm_assisted", required = false) final Boolean llmAssisted) {
		return ResponseEntity.ok(
				knowledgeMiddlewareProxy.postCodeToAMR(codeId.toString(), name, description, dynamicsOnly, llmAssisted)
						.getBody());
	}

	// Create a model from code blocks
	@Operation(summary = "Create a model from code blocks")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Return the extraction job for code to amr", content = @Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ExtractionResponse.class))),
			@ApiResponse(responseCode = "500", description = "Error running code blocks to model", content = @Content)
	})
	@PostMapping(value = "/code-blocks-to-model", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@Secured(Roles.USER)
	public ResponseEntity<ExtractionResponse> codeBlocksToModel(@RequestPart final Code code,
			@RequestPart("file") final MultipartFile input) throws IOException {

		try (final CloseableHttpClient httpClient = HttpClients.custom()
				.build()) {
			// 1. create code asset from code blocks
			final Code createdCode = codeService.createAsset(code);

			// 2. upload file to code asset
			final byte[] fileAsBytes = input.getBytes();
			final HttpEntity fileEntity = new ByteArrayEntity(fileAsBytes, ContentType.APPLICATION_OCTET_STREAM);

			// we have pre-formatted the files object already so no need to use uploadCode
			final PresignedURL presignedURL = codeService.getUploadUrl(createdCode.getId(),
					input.getOriginalFilename());
			final HttpPut put = new HttpPut(presignedURL.getUrl());
			put.setEntity(fileEntity);
			final HttpResponse response = httpClient.execute(put);

			if (response.getStatusLine().getStatusCode() != 200) {
				throw new Exception("Error uploading file");
			}
			// 3. create model from code asset
			return ResponseEntity.ok(knowledgeMiddlewareProxy
					.postCodeToAMR(createdCode.getId().toString(), "temp model", "temp model description", false, false)
					.getBody());
		} catch (final Exception e) {
			log.error("unable to upload file", e);
			throw new ResponseStatusException(
					org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
					"Error creating running code to model");
		}
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
			@RequestParam("document_id") final UUID documentId,
			@RequestParam(name = "annotate_skema", defaultValue = "true") final Boolean annotateSkema,
			@RequestParam(name = "annotate_mit", defaultValue = "true") final Boolean annotateMIT,
			@RequestParam(name = "name", required = false) final String name,
			@RequestParam(name = "description", required = false) final String description) {
		return ResponseEntity.ok(knowledgeMiddlewareProxy
				.postPDFExtractions(documentId.toString(), annotateSkema, annotateMIT, name, description).getBody());
	}

	/**
	 * Post a PDF to the extraction service to get text
	 *
	 * @param documentId (String): The ID of the document to extract text from
	 * @return response status of queueing this operation
	 */
	@PostMapping("/pdf-to-cosmos")
	@Secured(Roles.USER)
	public ResponseEntity<JsonNode> postPDFToCosmos(@RequestParam("document_id") final UUID documentId) {

		try {
			final JsonNode node = knowledgeMiddlewareProxy.postPDFToCosmos(documentId.toString()).getBody();

			return ResponseEntity.ok(node);
		} catch (final Exception e) {
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
			@PathVariable("model_id") final UUID modelId,
			@RequestParam("document_id") final UUID documentId) {

		try {
			final Provenance provenancePayload = new Provenance(ProvenanceRelationType.EXTRACTED_FROM, modelId,
					ProvenanceType.MODEL, documentId, ProvenanceType.DOCUMENT);
			provenanceService.createProvenance(provenancePayload);
		} catch (final Exception e) {
			final String error = "Unable to create provenance for profile-model";
			log.error(error, e);
		}

		return ResponseEntity
				.ok(knowledgeMiddlewareProxy.postProfileModel(modelId.toString(), documentId.toString()).getBody());
	}

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
			@PathVariable("dataset_id") final UUID datasetId,
			@RequestParam(name = "document_id", required = false) final Optional<UUID> documentId) {

		// Provenance call if a document id is provided
		if (documentId.isPresent()) {
			try {
				final Provenance provenancePayload = new Provenance(ProvenanceRelationType.EXTRACTED_FROM, datasetId,
						ProvenanceType.DATASET, documentId.get(), ProvenanceType.DOCUMENT);
				provenanceService.createProvenance(provenancePayload);
			} catch (final Exception e) {
				final String error = "Unable to create provenance for profile-dataset";
				log.error(error, e);
			}
		}

		final String docIdString = documentId.map(UUID::toString).orElse(null);
		return ResponseEntity.ok(
				knowledgeMiddlewareProxy.postProfileDataset(datasetId.toString(), docIdString).getBody());
	}

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
			@RequestParam("document_id") final String documentId,
			@RequestParam("model_id") final String modelId) {
		return ResponseEntity.ok(knowledgeMiddlewareProxy.postLinkAmr(documentId, modelId).getBody());
	}

}
