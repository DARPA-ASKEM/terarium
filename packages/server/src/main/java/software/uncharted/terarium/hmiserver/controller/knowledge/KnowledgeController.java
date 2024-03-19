package software.uncharted.terarium.hmiserver.controller.knowledge;

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
import org.apache.http.HttpEntity;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import software.uncharted.terarium.hmiserver.models.dataservice.Grounding;
import software.uncharted.terarium.hmiserver.models.dataservice.code.Code;
import software.uncharted.terarium.hmiserver.models.dataservice.code.CodeFile;
import software.uncharted.terarium.hmiserver.models.dataservice.dataset.Dataset;
import software.uncharted.terarium.hmiserver.models.dataservice.dataset.DatasetColumn;
import software.uncharted.terarium.hmiserver.models.dataservice.document.DocumentAsset;
import software.uncharted.terarium.hmiserver.models.dataservice.document.DocumentExtraction;
import software.uncharted.terarium.hmiserver.models.dataservice.document.ExtractionAssetType;
import software.uncharted.terarium.hmiserver.models.dataservice.model.Model;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.ModelHeader;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.ModelMetadata;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.metadata.Card;
import software.uncharted.terarium.hmiserver.models.dataservice.provenance.Provenance;
import software.uncharted.terarium.hmiserver.models.dataservice.provenance.ProvenanceQueryParam;
import software.uncharted.terarium.hmiserver.models.dataservice.provenance.ProvenanceRelationType;
import software.uncharted.terarium.hmiserver.models.dataservice.provenance.ProvenanceType;
import software.uncharted.terarium.hmiserver.models.extractionservice.ExtractionResponse;
import software.uncharted.terarium.hmiserver.proxies.documentservice.ExtractionProxy;
import software.uncharted.terarium.hmiserver.proxies.mit.MitProxy;
import software.uncharted.terarium.hmiserver.proxies.skema.SkemaUnifiedProxy;
import software.uncharted.terarium.hmiserver.proxies.skema.SkemaUnifiedProxy.IntegratedTextExtractionsBody;
import software.uncharted.terarium.hmiserver.security.Roles;
import software.uncharted.terarium.hmiserver.service.data.*;
import software.uncharted.terarium.hmiserver.utils.ByteMultipartFile;
import software.uncharted.terarium.hmiserver.utils.JsonUtil;
import software.uncharted.terarium.hmiserver.utils.StringMultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

@RequestMapping("/knowledge")
@RestController
@Slf4j
@RequiredArgsConstructor
public class KnowledgeController {

	final ObjectMapper mapper;

	final SkemaUnifiedProxy skemaUnifiedProxy;
	final MitProxy mitProxy;
	final ExtractionProxy extractionProxy;

	final DocumentAssetService documentService;
	final DatasetService datasetService;
	final ModelService modelService;
	final ProvenanceService provenanceService;
	final ProvenanceSearchService provenanceSearchService;

	final CodeService codeService;

	@Value("${mit-openai-api-key:}")
	String MIT_OPENAI_API_KEY;

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
			// Catch every exception thrown by the Proxy
		} catch (final FeignException e) {
			// If the Skema Unified Service does not return a 2xx status code, we throw a
			// 500 error
			final int status = e.status() < 400 ? 500 : e.status();
			throw new ResponseStatusException(
					HttpStatus.valueOf(status),
					"Skema Unified Service did not return any AMR based on the provided Equations. \n"
							+ e.getMessage());
		} catch (final Exception e) {
			throw new ResponseStatusException(
					HttpStatus.INTERNAL_SERVER_ERROR,
					"Unable to reach Skema Unified Service. " + e.getMessage());
		}

		final String serviceSuccessMessage = "Skema Unified Service returned an AMR based on the provided Equations. ";

		// If no model id is provided, create a new model
		UUID modelId = null;
		final String modelIdString = req.get("modelId") != null ? req.get("modelId").asText() : null;
		if (modelIdString != null) {
			try {
				// Get the model id if it is a valid UUID
				modelId = UUID.fromString(modelIdString);
			} catch (final IllegalArgumentException e) {
				throw new ResponseStatusException(
						HttpStatus.BAD_REQUEST,
						serviceSuccessMessage + "The provided modelId is not a valid UUID.");
			}
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
	 *                     model
	 * @param dynamicsOnly (Boolean): whether to only run the amr extraction over
	 *                     specified dynamics from the code object in TDS
	 * @param llmAssisted  (Boolean): whether amr extraction is llm assisted
	 * @return Model
	 */
	@PostMapping("/code-to-amr")
	@Secured(Roles.USER)
	ResponseEntity<Model> postCodeToAMR(
			@RequestParam("code-id") final UUID codeId,
			@RequestParam(name = "dynamics-only", required = false, defaultValue = "false") Boolean dynamicsOnly,
			@RequestParam(name = "llm-assisted", required = false, defaultValue = "false") final Boolean llmAssisted) {

		try {

			final Code code = codeService.getAsset(codeId).orElseThrow();
			final Map<String, CodeFile> codeFiles = code.getFiles();

			final Map<String, String> codeContent = new HashMap<>();

			for (final Entry<String, CodeFile> file : codeFiles.entrySet()) {
				final String filename = file.getKey();
				final CodeFile codeFile = file.getValue();
				final String content = codeService.fetchFileAsString(codeId, filename).orElseThrow();

				if (dynamicsOnly && codeFile.getDynamics() != null && codeFile.getDynamics().getBlock() != null) {
					final List<String> blocks = codeFile.getDynamics().getBlock();
					for (final String block : blocks) {
						final String[] parts = block.split("-");
						final int startLine = Integer.parseInt(parts[0].substring(1));
						final int endLine = Integer.parseInt(parts[1].substring(1));

						final String[] codeLines = content.split("\n");
						final List<String> targetLines = Arrays.asList(codeLines).subList(startLine - 1, endLine);

						final String targetBlock = String.join("\n", targetLines);

						codeContent.put(filename, targetBlock);
					}
				} else {
					codeContent.put(filename, content);
					dynamicsOnly = false;
				}
			}

			final List<String> files = new ArrayList<>();
			final List<String> blobs = new ArrayList<>();

			ResponseEntity<JsonNode> resp = null;

			try {
				if (dynamicsOnly) {
					for (final Entry<String, String> entry : codeContent.entrySet()) {
						files.add(entry.getKey());
						blobs.add(entry.getValue());
					}

					resp = skemaUnifiedProxy.snippetsToAMR(files, blobs);

				} else {
					final ByteArrayOutputStream zipBuffer = new ByteArrayOutputStream();
					final ZipOutputStream zipf = new ZipOutputStream(zipBuffer, StandardCharsets.UTF_8);

					for (final Map.Entry<String, String> entry : codeContent.entrySet()) {
						final String codeName = entry.getKey();
						final String content = entry.getValue();
						final ZipEntry zipEntry = new ZipEntry(codeName);
						zipf.putNextEntry(zipEntry);
						zipf.write(content.getBytes(StandardCharsets.UTF_8));
						zipf.closeEntry();
					}
					zipf.close();

					final ByteMultipartFile file = new ByteMultipartFile(zipBuffer.toByteArray(), "zip_file.zip",
							"application/zip");

					resp = llmAssisted ? skemaUnifiedProxy.llmCodebaseToAMR(file)
							: skemaUnifiedProxy.codebaseToAMR(file);

				}
			} catch (final FeignException e) {
				log.error("SKEMA was unable to create a model with the code provided", e);
				throw new ResponseStatusException(
						org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY,
						"Unable to get code to amr");
			}

			if (!resp.getStatusCode().is2xxSuccessful()) {
				throw new RuntimeException("Unable to get code to amr: " + resp.getBody().asText());
			}

			Model model = mapper.treeToValue(resp.getBody(), Model.class);

			if (model.getMetadata() == null) {
				model.setMetadata(new ModelMetadata());
			}

			// create the model
			if (model.getMetadata() == null) {
				model.setMetadata(new ModelMetadata());
			}
			model.getMetadata().setCodeId(codeId.toString());
			model = modelService.createAsset(model);

			// update the code
			if (code.getMetadata() == null) {
				code.setMetadata(new HashMap<>());
			}
			code.getMetadata().put("model_id", model.getId().toString());
			codeService.updateAsset(code);

			// set the provenance
			final Provenance provenancePayload = new Provenance(ProvenanceRelationType.EXTRACTED_FROM, model.getId(),
					ProvenanceType.MODEL, codeId, ProvenanceType.CODE);
			provenanceService.createProvenance(provenancePayload);

			return ResponseEntity.ok(model);

		} catch (final Exception e) {
			log.error("Unable to get code to amr", e);
			throw new ResponseStatusException(
					org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
					"Unable to get code to amr");
		}
	}

	// Create a model from code blocks
	@Operation(summary = "Create a model from code blocks")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Return the extraction job for code to amr", content = @Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ExtractionResponse.class))),
			@ApiResponse(responseCode = "500", description = "Error running code blocks to model", content = @Content)
	})
	@PostMapping(value = "/code-blocks-to-model", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@Secured(Roles.USER)
	public ResponseEntity<Model> codeBlocksToModel(@RequestPart final Code code,
			@RequestPart("file") final MultipartFile input) throws IOException {

		try (final CloseableHttpClient httpClient = HttpClients.custom()
				.build()) {
			// 1. create code asset from code blocks
			final Code createdCode = codeService.createAsset(code);

			// 2. upload file to code asset
			final byte[] fileAsBytes = input.getBytes();
			final HttpEntity fileEntity = new ByteArrayEntity(fileAsBytes, ContentType.APPLICATION_OCTET_STREAM);
			final String filename = input.getOriginalFilename();

			codeService.uploadFile(code.getId(), filename, fileEntity);

			// add the code file to the code asset
			final CodeFile codeFile = new CodeFile();
			codeFile.setProgrammingLanguageFromFileName(filename);

			if (code.getFiles() == null) {
				code.setFiles(new HashMap<>());
			}
			code.getFiles().put(filename, codeFile);
			codeService.updateAsset(code);

			// 3. create model from code asset
			return postCodeToAMR(createdCode.getId(), false, false);
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
	@PostMapping("/profile-model/{model-id}")
	@Secured(Roles.USER)
	public ResponseEntity<Model> postProfileModel(
			@PathVariable("model-id") final UUID modelId,
			@RequestParam("document-id") final UUID documentId) {

		try {
			final Provenance provenancePayload = new Provenance(ProvenanceRelationType.EXTRACTED_FROM, modelId,
					ProvenanceType.MODEL, documentId, ProvenanceType.DOCUMENT);
			provenanceService.createProvenance(provenancePayload);
		} catch (final Exception e) {
			final String error = "Unable to create provenance for profile-model";
			log.error(error, e);
		}

		try {
			final ProvenanceQueryParam payload = new ProvenanceQueryParam();
			payload.setRootId(modelId);
			payload.setRootType(ProvenanceType.MODEL);

			final Set<String> codeIds = provenanceSearchService.modelsFromCode(payload);

			String codeContentString = "No available code associated with model";
			if (codeIds.size() > 0) {
				final UUID codeId = UUID.fromString(codeIds.iterator().next());

				final Code code = codeService.getAsset(codeId).orElseThrow();

				final Map<String, String> codeContent = new HashMap<>();

				for (final Entry<String, CodeFile> file : code.getFiles().entrySet()) {

					final String name = file.getKey();
					final String content = codeService.fetchFileAsString(codeId, file.getKey()).orElseThrow();

					codeContent.put(name, content);
				}
				codeContentString = mapper.writeValueAsString(codeContent);
			}

			final Optional<DocumentAsset> documentOptional = documentService.getAsset(documentId);
			String documentText = "There is no documentation for this model";
			if (documentOptional.isPresent()) {
				final int MAX_CHAR_LIMIT = 9000;

				final DocumentAsset document = documentOptional.get();
				documentText = document.getText().substring(0, Math.min(document.getText().length(), MAX_CHAR_LIMIT));
			}

			final Model model = modelService.getAsset(modelId).orElseThrow();

			final StringMultipartFile textFile = new StringMultipartFile(documentText, "document.txt",
					"application/text");
			final StringMultipartFile codeFile = new StringMultipartFile(codeContentString, "code.txt",
					"application/text");

			final ResponseEntity<JsonNode> resp = mitProxy.modelCard(MIT_OPENAI_API_KEY, textFile, codeFile);
			if (!resp.getStatusCode().is2xxSuccessful()) {
				throw new RuntimeException("Unable to get model card: " + resp.getBody().asText());
			}

			final Card card = mapper.treeToValue(resp.getBody(), Card.class);

			if (model.getHeader() == null) {
				model.setHeader(new ModelHeader());
			}

			if (model.getMetadata() == null) {
				model.setMetadata(new ModelMetadata());
			}

			model.getHeader().setDescription(card.getDescription());
			model.getMetadata().setCard(card);

			return ResponseEntity.ok(modelService.updateAsset(model).orElseThrow());

		} catch (final Exception e) {
			log.error("Unable to get profile model", e);
			throw new ResponseStatusException(
					org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
					"Unable to get profile model");
		}
	}

	/**
	 * Profile a dataset
	 *
	 * @param datasetId  (String): The ID of the dataset to profile
	 * @param documentId (String): The ID of the document to profile
	 * @return the profiled dataset
	 */
	@PostMapping("/profile-dataset/{dataset-id}")
	@Secured(Roles.USER)
	public ResponseEntity<Dataset> postProfileDataset(
			@PathVariable("dataset-id") final UUID datasetId,
			@RequestParam(name = "document-id", required = false) final Optional<UUID> documentId) {

		try {
			// Provenance call if a document id is provided
			StringMultipartFile documentFile = null;
			if (documentId.isPresent()) {

				final DocumentAsset document = documentService.getAsset(documentId.get()).orElseThrow();
				documentFile = new StringMultipartFile(document.getText(), documentId.get() + ".txt",
						"application/text");

				try {
					final Provenance provenancePayload = new Provenance(ProvenanceRelationType.EXTRACTED_FROM,
							datasetId,
							ProvenanceType.DATASET, documentId.get(), ProvenanceType.DOCUMENT);
					provenanceService.createProvenance(provenancePayload);

				} catch (final Exception e) {
					final String error = "Unable to create provenance for profile-dataset";
					log.error(error, e);
				}
			} else {
				documentFile = new StringMultipartFile("There is no documentation for this dataset",
						documentId.get() + ".txt", "application/text");
			}

			final Dataset dataset = datasetService.getAsset(datasetId).orElseThrow();

			if (dataset.getFileNames().isEmpty()) {
				throw new RuntimeException("No files found on dataset");
			}
			final String filename = dataset.getFileNames().get(0);

			final String csvContents = datasetService.fetchFileAsString(datasetId, filename).orElseThrow();

			final StringMultipartFile csvFile = new StringMultipartFile(csvContents, filename, "application/csv");

			final ResponseEntity<JsonNode> resp = mitProxy.dataCard(MIT_OPENAI_API_KEY, csvFile, documentFile);
			if (!resp.getStatusCode().is2xxSuccessful()) {
				throw new RuntimeException("Unable to get data card: " + resp.getBody().asText());
			}

			final JsonNode card = resp.getBody();
			final JsonNode profilingResult = card.get("DATA_PROFILING_RESULT");

			final List<DatasetColumn> columns = new ArrayList<>();
			for (final DatasetColumn col : dataset.getColumns()) {

				final JsonNode annotation = profilingResult.get(col.getName());
				if (annotation == null) {
					log.warn("No annotations for column: {}", col.getName());
					continue;
				}

				final JsonNode dkgGroundings = annotation.get("dkg_groundings");
				if (dkgGroundings == null) {
					log.warn("No dkg_groundings for column: {}", col.getName());
					continue;
				}

				final Grounding groundings = new Grounding();
				for (final JsonNode g : annotation.get("dkg_groundings")) {
					if (g.size() < 2) {
						log.warn("Invalid dkg_grounding: {}", g);
						continue;
					}
					if (groundings.getIdentifiers() == null) {
						groundings.setIdentifiers(new HashMap<>());
					}
					groundings.getIdentifiers().put(g.get(0).asText(), g.get(1).asText());
				}

				// remove groundings from annotation object
				((ObjectNode) annotation).remove("dkg_groundings");

				final DatasetColumn newCol = new DatasetColumn();
				newCol.setName(col.getName());
				newCol.setDataType(col.getDataType());
				newCol.setFormatStr(col.getFormatStr());
				newCol.setGrounding(col.getGrounding());
				newCol.setAnnotations(col.getAnnotations());
				newCol.setDescription(annotation.get("description").asText());
				newCol.setMetadata(mapper.convertValue(annotation, Map.class));
				columns.add(newCol);
			}

			dataset.setColumns(columns);


			if (dataset.getMetadata() != null) {
				dataset.setMetadata(mapper.convertValue(Map.of("dataCard", card), JsonNode.class));
			} else {
				final ObjectNode metadata = mapper.createObjectNode();
				((ObjectNode)dataset.getMetadata()).putPOJO("dataCard", card);
			}



			return ResponseEntity.ok(datasetService.updateAsset(dataset).orElseThrow());

		} catch (final Exception e) {
			final String error = "Unable to get profile dataset";
			log.error(error, e);
			throw new ResponseStatusException(
					org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
					error);
		}
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
				log.error("MIT variable extraction for document {} failed", documentId, e);
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

	public static HttpEntity zipEntryToHttpEntity(final ZipInputStream zipInputStream) throws IOException {
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
