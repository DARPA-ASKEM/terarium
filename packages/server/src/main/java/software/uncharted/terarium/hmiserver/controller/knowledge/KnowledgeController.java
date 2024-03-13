package software.uncharted.terarium.hmiserver.controller.knowledge;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import feign.FeignException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.uncharted.terarium.hmiserver.models.dataservice.PresignedURL;
import software.uncharted.terarium.hmiserver.models.dataservice.code.Code;
import software.uncharted.terarium.hmiserver.models.dataservice.document.DocumentAsset;
import software.uncharted.terarium.hmiserver.models.dataservice.document.DocumentExtraction;
import software.uncharted.terarium.hmiserver.models.dataservice.document.ExtractionAssetType;
import software.uncharted.terarium.hmiserver.models.dataservice.model.Model;
import software.uncharted.terarium.hmiserver.models.dataservice.provenance.Provenance;
import software.uncharted.terarium.hmiserver.models.dataservice.provenance.ProvenanceRelationType;
import software.uncharted.terarium.hmiserver.models.dataservice.provenance.ProvenanceType;
import software.uncharted.terarium.hmiserver.models.extractionservice.ExtractionResponse;
import software.uncharted.terarium.hmiserver.proxies.documentservice.ExtractionProxy;
import software.uncharted.terarium.hmiserver.proxies.knowledge.KnowledgeMiddlewareProxy;
import software.uncharted.terarium.hmiserver.proxies.mit.MitProxy;
import software.uncharted.terarium.hmiserver.proxies.skema.SkemaUnifiedProxy;
import software.uncharted.terarium.hmiserver.proxies.skema.SkemaUnifiedProxy.IntegratedTextExtractionsBody;
import software.uncharted.terarium.hmiserver.security.Roles;
import software.uncharted.terarium.hmiserver.service.data.CodeService;
import software.uncharted.terarium.hmiserver.service.data.DocumentAssetService;
import software.uncharted.terarium.hmiserver.service.data.ModelService;
import software.uncharted.terarium.hmiserver.service.data.ProvenanceService;
import software.uncharted.terarium.hmiserver.utils.ByteMultipartFile;
import software.uncharted.terarium.hmiserver.utils.JsonUtil;
import software.uncharted.terarium.hmiserver.utils.StringMultipartFile;

@RequestMapping("/knowledge")
@RestController
@Slf4j
@RequiredArgsConstructor
public class KnowledgeController {

	final ObjectMapper mapper;

	final KnowledgeMiddlewareProxy knowledgeMiddlewareProxy;

	final SkemaUnifiedProxy skemaUnifiedProxy;
	final MitProxy mitProxy;
	final ExtractionProxy extractionProxy;

	final DocumentAssetService documentService;
	final ModelService modelService;
	final ProvenanceService provenanceService;

	final CodeService codeService;

	@Value("${mit-openai-api-key:}")
	String MIT_OPENAI_API_KEY;

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
	 *
	 * @return UUID Model ID, or null if the model was not created or updated
	 */
	@PostMapping("/equations-to-model")
	@Secured(Roles.USER)
	public ResponseEntity<UUID> equationsToModel(@RequestBody final JsonNode req) {
		final Model responseAMR;

		// Get an AMR from Skema Unified Service
		try {
			responseAMR = skemaUnifiedProxy
					.consolidatedEquationsToAMR(req)
					.getBody();

			if (responseAMR == null) {
				throw new ResponseStatusException(
						HttpStatus.UNPROCESSABLE_ENTITY,
						"Skema Unified Service did not return any AMR based on the provided Equations. This could be due to invalid equations or the inability to parse them into the requested framework.");
			}
		} catch (final FeignException e) {
			throw new ResponseStatusException(
					HttpStatus.valueOf(e.status()),
					"Skema Unified Service did not return any AMR based on the provided Equations. " + e.getMessage());
		}

		final String serviceSuccessMessage = "Skema Unified Service returned an AMR based on the provided Equations. ";

		// If no model id is provided, create a new model
		UUID modelId = null;
		try {
			modelId = req.get("modelId") != null ? UUID.fromString(req.get("modelId").asText()) : null;
		} catch (final IllegalArgumentException e) {
			throw new ResponseStatusException(
					HttpStatus.BAD_REQUEST,
					serviceSuccessMessage + "The provided modelId is not a valid UUID.");
		}

		if (modelId == null) {
			try {
				final Model model = modelService.createAsset(responseAMR);
				return ResponseEntity.ok(model.getId());
			} catch (final IOException e) {
				log.error("Unable to create a model", e);
				throw new ResponseStatusException(
						org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
						serviceSuccessMessage
								+ "However, we encountered an issue creating the model. Please try again.");
			}
		}

		// If a model id is provided, update the model
		try {
			final Optional<Model> model = modelService.getAsset(modelId);
			if (model.isEmpty()) {
				final String errorMessage = String.format("The model id %s does not exist.", modelId);
				log.error(errorMessage);
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, errorMessage);
			}
			responseAMR.setId(model.get().getId());
			modelService.updateAsset(responseAMR);
			return ResponseEntity.ok(model.get().getId());

		} catch (final IOException e) {
			log.error("Unable to update the model id {}.", modelId, e);
			throw new ResponseStatusException(
					org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
					serviceSuccessMessage + "However, we encountered an issue updating the model. Please try again.");
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

	@PostMapping("/link-amr")
	@Secured(Roles.USER)
	public ResponseEntity<Model> postLinkAmr(
			@RequestParam("document-id") final UUID documentId,
			@RequestParam("model-id") final UUID modelId) {

		try {
			final DocumentAsset document = documentService.getAsset(documentId).orElseThrow();

			final Model model = modelService.getAsset(modelId).orElseThrow();

			final String modelString = mapper.writeValueAsString(model);
			final String extractionsString = mapper
					.writeValueAsString(document.getMetadata() != null ? document.getMetadata() : new HashMap<>());

			final StringMultipartFile amrFile = new StringMultipartFile(modelString, "amr.json",
					"application/json");
			final StringMultipartFile extractionFile = new StringMultipartFile(
					extractionsString, "extractions.json",
					"application/json");

			final ResponseEntity<JsonNode> res = skemaUnifiedProxy.linkAMRFile(amrFile, extractionFile);
			if (!res.getStatusCode().is2xxSuccessful()) {
				throw new RuntimeException("Unable to link AMR file: " + res.getBody().asText());
			}

			final JsonNode modelJson = mapper.valueToTree(model);

			// ovewrite all updated fields
			JsonUtil.recursiveSetAll((ObjectNode) modelJson, (ObjectNode) res.getBody());

			// update the model
			modelService.updateAsset(model);

			// create provenance
			final Provenance provenance = new Provenance(ProvenanceRelationType.EXTRACTED_FROM, modelId,
					ProvenanceType.MODEL,
					documentId, ProvenanceType.DOCUMENT);
			provenanceService.createProvenance(provenance);

			return ResponseEntity.ok(model);

		} catch (final Exception e) {
			final String error = "Unable to get link amr";
			log.error(error, e);
			throw new ResponseStatusException(
					org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
					error);
		}
	}

	@PostMapping("/variable-extractions")
	ResponseEntity<DocumentAsset> postPdfExtractions(
			@RequestParam("document-id") final UUID documentId,
			@RequestParam(name = "annotate-skema", defaultValue = "true") final Boolean annotateSkema,
			@RequestParam(name = "annotate-mit", defaultValue = "true") final Boolean annotateMIT,
			@RequestParam(name = "domain", defaultValue = "epi") final String domain) {
		try {

			DocumentAsset document = documentService.getAsset(documentId).orElseThrow();

			if (document.getText() == null || document.getText().isEmpty()) {
				throw new RuntimeException(
						"No text found in paper document, please ensure to submit to /variable-extractions endpoint.");
			}

			final List<JsonNode> collections = new ArrayList<>();
			JsonNode skemaCollection = null;
			JsonNode mitCollection = null;

			// Send document to SKEMA
			try {
				final IntegratedTextExtractionsBody body = new IntegratedTextExtractionsBody(document.getText());

				final ResponseEntity<JsonNode> resp = skemaUnifiedProxy.integratedTextExtractions(annotateMIT,
						annotateSkema, body);

				if (resp.getStatusCode().is2xxSuccessful()) {
					for (final JsonNode output : resp.getBody().get("outputs")) {
						if (!output.has("errors") || output.get("errors").size() == 0) {
							skemaCollection = output.get("data");
							break;
						}
					}

					if (skemaCollection != null) {
						collections.add(skemaCollection);
					}
				} else {
					log.error("Unable to extract variables from document: " + document.getId());
				}

			} catch (final Exception e) {
				log.error("SKEMA variable extraction for document " + documentId + " failed.", e);
			}

			// Send document to MIT
			try {
				final StringMultipartFile file = new StringMultipartFile(document.getText(), "text.txt",
						"application/text");

				final ResponseEntity<JsonNode> resp = mitProxy.uploadFileExtract(MIT_OPENAI_API_KEY, domain, file);

				if (resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null) {
					mitCollection = resp.getBody();
					collections.add(mitCollection);
				} else {
					log.error("Unable to extract variables from document: " + document.getId());
				}

			} catch (final Exception e) {
				log.error("SKEMA variable extraction for document {} failed", documentId, e);
			}

			if (skemaCollection == null && mitCollection == null) {
				throw new RuntimeException("Unable to extract variables from document: " + document.getId());
			}

			final List<JsonNode> attributes = new ArrayList<>();

			if (skemaCollection == null || mitCollection == null) {
				log.info("Falling back on single variable extraction since one system failed");
				for (final JsonNode collection : collections) {
					for (final JsonNode attribute : collection.get("attributes")) {
						attributes.add(attribute);
					}
				}
			} else {
				// Merge both with some de de-duplications

				final StringMultipartFile arizonaFile = new StringMultipartFile(
						mapper.writeValueAsString(skemaCollection),
						"text.json",
						"application/json");

				final StringMultipartFile mitFile = new StringMultipartFile(
						mapper.writeValueAsString(mitCollection),
						"text.json",
						"application/json");

				final ResponseEntity<JsonNode> resp = mitProxy.getMapping(MIT_OPENAI_API_KEY, domain, mitFile,
						arizonaFile);

				if (resp.getStatusCode().is2xxSuccessful()) {
					for (final JsonNode attribute : resp.getBody().get("attributes")) {
						attributes.add(attribute);
					}
				} else {
					// fallback to collection
					log.info("MIT merge failed: {}", resp.getBody().asText());
					for (final JsonNode collection : collections) {
						for (final JsonNode attribute : collection.get("attributes")) {
							attributes.add(attribute);
						}
					}
				}
			}

			// add the attributes to the metadata
			if (document.getMetadata() == null) {
				document.setMetadata(new HashMap<>());
			}
			document.getMetadata().put("attributes", attributes);

			// update the document
			document = documentService.updateAsset(document).orElseThrow();

			return ResponseEntity.ok(document);

		} catch (final IOException e) {
			final String error = "Unable to get required assets";
			log.error(error, e);
			throw new ResponseStatusException(
					org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
					error);
		}
	}

	public HttpEntity zipEntryToHttpEntity(final ZipInputStream zipInputStream) throws IOException {
		final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		final byte[] buffer = new byte[1024];
		int len;
		while ((len = zipInputStream.read(buffer)) > 0) {
			byteArrayOutputStream.write(buffer, 0, len);
		}

		return new ByteArrayEntity(byteArrayOutputStream.toByteArray());
	}

	@PostMapping("/pdf-to-cosmos")
	@Secured(Roles.USER)
	public ResponseEntity<DocumentAsset> postPDFToCosmos(
			@RequestParam("document-id") final UUID documentId) {

		try {
			DocumentAsset document = documentService.getAsset(documentId).orElseThrow();

			if (document.getFileNames().isEmpty()) {
				throw new RuntimeException("No files found on document");
			}

			final String filename = document.getFileNames().get(0);

			final byte[] documentContents = documentService.fetchFileAsBytes(documentId, filename).orElseThrow();

			final ByteMultipartFile documentFile = new ByteMultipartFile(documentContents, filename,
					"application/pdf");

			final boolean compressImages = false;
			final boolean forceRun = false;
			final ResponseEntity<JsonNode> extractionResp = extractionProxy.processPdfExtraction(compressImages,
					!forceRun,
					documentFile);

			final JsonNode body = extractionResp.getBody();
			final UUID jobId = UUID.fromString(body.get("job_id").asText());

			final int POLLING_INTERVAL_SECONDS = 5;
			final int MAX_EXECUTION_TIME_SECONDS = 600;
			final int MAX_ITERATIONS = MAX_EXECUTION_TIME_SECONDS / POLLING_INTERVAL_SECONDS;

			boolean jobDone = false;
			final RestTemplate restTemplate = new RestTemplate();
			for (int i = 0; i < MAX_ITERATIONS; i++) {

				final ResponseEntity<JsonNode> statusResp = extractionProxy.status(jobId);
				if (!statusResp.getStatusCode().is2xxSuccessful()) {
					throw new RuntimeException("Unable to poll status endpoint");
				}

				final JsonNode statusData = statusResp.getBody();
				if (!statusData.get("error").isNull()) {
					throw new RuntimeException("Extraction job failed: " + statusData.has("error"));
				}

				log.info("Polled status endpoint {} times:\n{}", i + 1, statusData);
				jobDone = statusData.get("error").asBoolean() || statusData.get("job_completed").asBoolean();
				if (jobDone) {
					break;
				}
				Thread.sleep(POLLING_INTERVAL_SECONDS * 1000);
			}

			if (!jobDone) {
				throw new RuntimeException("Extraction job did not complete within the expected time");
			}

			final ResponseEntity<byte[]> zipFileResp = extractionProxy.result(jobId);
			if (!zipFileResp.getStatusCode().is2xxSuccessful()) {
				throw new RuntimeException("Unable to fetch the extraction result");
			}

			final String zipFileName = documentId + "_cosmos.zip";
			documentService.uploadFile(documentId, zipFileName, new ByteArrayEntity(zipFileResp.getBody()));

			document.getFileNames().add(zipFileName);

			// Open the zipfile and extract the contents

			final Map<String, HttpEntity> fileMap = new HashMap<>();
			try {
				final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(zipFileResp.getBody());
				final ZipInputStream zipInputStream = new ZipInputStream(byteArrayInputStream);

				ZipEntry entry = zipInputStream.getNextEntry();
				while (entry != null) {

					fileMap.put(entry.getName(), zipEntryToHttpEntity(zipInputStream));

					entry = zipInputStream.getNextEntry();
				}

				zipInputStream.closeEntry();
				zipInputStream.close();
			} catch (final IOException e) {
				throw new RuntimeException("Unable to extract the contents of the zip file", e);
			}

			final ResponseEntity<JsonNode> textResp = extractionProxy.text(jobId);
			if (!textResp.getStatusCode().is2xxSuccessful()) {
				throw new RuntimeException("Unable to fetch the text extractions");
			}

			// clear existing assets
			document.setAssets(new ArrayList<>());

			for (final ExtractionAssetType extractionType : ExtractionAssetType.values()) {
				final ResponseEntity<JsonNode> response = extractionProxy.extraction(jobId,
						extractionType.toStringPlural());
				log.info(" {} response status: {}", extractionType, response.getStatusCode());
				if (!response.getStatusCode().is2xxSuccessful()) {
					log.warn("Unable to fetch the {} extractions", extractionType);
					continue;
				}

				for (final JsonNode record : response.getBody()) {

					String fileName = "";
					if (record.has("img_pth")) {

						final String path = record.get("img_pth").asText();
						fileName = path.substring(path.lastIndexOf("/") + 1);

						if (fileMap.containsKey(fileName)) {
							log.warn("Unable to find file {} in zipfile", fileName);
						}

						final HttpEntity file = fileMap.get(fileName);

						documentService.uploadFile(documentId, fileName, file);

					} else {
						log.warn("No img_pth found in record: {}", record);
					}

					final DocumentExtraction extraction = new DocumentExtraction();
					extraction.setFileName(fileName);
					extraction.setAssetType(extractionType);
					extraction.setMetadata(mapper.convertValue(record, Map.class));

					document.getAssets().add(extraction);
				}
			}

			String responseText = "";
			for (final JsonNode record : textResp.getBody()) {
				if (record.has("content")) {
					responseText += record.get("content").asText() + "\n";
				} else {
					log.warn("No content found in record: {}", record);
				}
			}

			document.setText(responseText);

			// update the document
			document = documentService.updateAsset(document).orElseThrow();

			return ResponseEntity.ok(document);

		} catch (final Exception e) {
			final String error = "Unable to extract pdf";
			log.error(error, e);
			throw new ResponseStatusException(
					org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
					error);
		}
	}

}
