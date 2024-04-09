package software.uncharted.terarium.hmiserver.controller.knowledge;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.http.HttpEntity;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
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
import software.uncharted.terarium.hmiserver.models.dataservice.Grounding;
import software.uncharted.terarium.hmiserver.models.dataservice.code.Code;
import software.uncharted.terarium.hmiserver.models.dataservice.code.CodeFile;
import software.uncharted.terarium.hmiserver.models.dataservice.dataset.Dataset;
import software.uncharted.terarium.hmiserver.models.dataservice.dataset.DatasetColumn;
import software.uncharted.terarium.hmiserver.models.dataservice.document.DocumentAsset;
import software.uncharted.terarium.hmiserver.models.dataservice.model.Model;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.ModelHeader;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.ModelMetadata;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.metadata.Card;
import software.uncharted.terarium.hmiserver.models.dataservice.provenance.Provenance;
import software.uncharted.terarium.hmiserver.models.dataservice.provenance.ProvenanceRelationType;
import software.uncharted.terarium.hmiserver.models.dataservice.provenance.ProvenanceType;
import software.uncharted.terarium.hmiserver.models.extractionservice.ExtractionResponse;
import software.uncharted.terarium.hmiserver.proxies.mit.MitProxy;
import software.uncharted.terarium.hmiserver.proxies.skema.SkemaUnifiedProxy;
import software.uncharted.terarium.hmiserver.security.Roles;
import software.uncharted.terarium.hmiserver.service.ExtractionService;
import software.uncharted.terarium.hmiserver.service.data.CodeService;
import software.uncharted.terarium.hmiserver.service.data.DatasetService;
import software.uncharted.terarium.hmiserver.service.data.DocumentAssetService;
import software.uncharted.terarium.hmiserver.service.data.ModelService;
import software.uncharted.terarium.hmiserver.service.data.ProvenanceSearchService;
import software.uncharted.terarium.hmiserver.service.data.ProvenanceService;
import software.uncharted.terarium.hmiserver.utils.ByteMultipartFile;
import software.uncharted.terarium.hmiserver.utils.StringMultipartFile;

@RequestMapping("/knowledge")
@RestController
@Slf4j
@RequiredArgsConstructor
public class KnowledgeController {

	final ObjectMapper mapper;

	final SkemaUnifiedProxy skemaUnifiedProxy;
	final MitProxy mitProxy;

	final DocumentAssetService documentService;
	final DatasetService datasetService;
	final ModelService modelService;
	final ProvenanceService provenanceService;
	final ProvenanceSearchService provenanceSearchService;

	final CodeService codeService;

	final ExtractionService extractionService;

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
			final String error = "Skema Unified Service did not return any AMR based on the provided Equations";
			log.error(error, e);
			if (e.status() < 100)
				throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, error + ": " + e.getMessage());
			throw new ResponseStatusException(HttpStatus.valueOf(e.status()), error + ": " + e.getMessage());
		} catch (final Exception e) {
			final String error = "Unable to reach Skema Unified Service";
			log.error(error, e);
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, error + ": " + e.getMessage());
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
		try {
			return ResponseEntity
					.ok(skemaUnifiedProxy
							.base64EquationsToAMR(req)
							.getBody());
		} catch (final FeignException e) {
			final String error = "Error with Skema Unified Service while converting base64 equations to AMR";
			log.error(error, e);
			if (e.status() < 100)
				throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, error + ": " + e.getMessage());
			throw new ResponseStatusException(HttpStatus.valueOf(e.status()), error + ": " + e.getMessage());
		}
	}

	@PostMapping("/base64-equations-to-latex")
	@Secured(Roles.USER)
	public ResponseEntity<String> base64EquationsToLatex(@RequestBody final JsonNode req) {
		try {
			return ResponseEntity
					.ok(skemaUnifiedProxy
							.base64EquationsToLatex(req)
							.getBody());
		} catch (final FeignException e) {
			final String error = "Error with Skema Unified Service while converting base64 equations to Latex";
			log.error(error, e);
			if (e.status() < 100)
				throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, error + ": " + e.getMessage());
			throw new ResponseStatusException(HttpStatus.valueOf(e.status()), error + ": " + e.getMessage());
		}
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
			@RequestParam(name = "name", required = false, defaultValue = "") final String name,
			@RequestParam(name = "description", required = false, defaultValue = "") final String description,
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
				final String error = "SKEMA was unable to create a model with the code provided";
				log.error(error, e);
				throw new ResponseStatusException(
						e.status() < 100 ? org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY
								: HttpStatus.valueOf(e.status()),
						error + ": " + e.getMessage());
			} catch (final Exception e) {
				log.error("Unable to get code to amr", e);
				throw new ResponseStatusException(
						org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
						"Unable to get code to amr: " + e.getMessage());
			}

			if (!resp.getStatusCode().is2xxSuccessful()) {
				throw new ResponseStatusException(
						resp.getStatusCode(),
						"Unable to get code to amr from SKEMA");
			}

			Model model = mapper.treeToValue(resp.getBody(), Model.class);

			if (model.getMetadata() == null) {
				model.setMetadata(new ModelMetadata());
			}

			// create the model
			if (!name.isEmpty()) {
				model.setName(name);
			}
			if (model.getMetadata() == null) {
				model.setMetadata(new ModelMetadata());
			}
			model.getMetadata().setCodeId(codeId.toString());

			if (!description.isEmpty()) {
				if (model.getHeader() == null) {
					model.setHeader(new ModelHeader());
				}
				model.getHeader().setDescription(description);
			}
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

		} catch (final IOException e) {
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
			@ApiResponse(responseCode = "400", description = "Invalid input - code file may be missing name", content = @Content),
			@ApiResponse(responseCode = "500", description = "Error running code blocks to model", content = @Content)
	})
	@PostMapping(value = "/code-blocks-to-model", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@Secured(Roles.USER)
	public ResponseEntity<Model> codeBlocksToModel(@RequestPart final Code code,
			@RequestPart("file") final MultipartFile input) throws IOException {

		try {
			// 1. create code asset from code blocks
			final Code createdCode = codeService.createAsset(code);

			// 2. upload file to code asset
			final byte[] fileAsBytes = input.getBytes();
			final HttpEntity fileEntity = new ByteArrayEntity(fileAsBytes, ContentType.APPLICATION_OCTET_STREAM);
			final String filename = input.getOriginalFilename();

			if (filename == null) {
				throw new ResponseStatusException(
						HttpStatus.BAD_REQUEST,
						"File name is required");
			}

			codeService.uploadFile(code.getId(), filename, fileEntity, ContentType.TEXT_PLAIN);

			// add the code file to the code asset
			final CodeFile codeFile = new CodeFile();
			codeFile.setProgrammingLanguageFromFileName(filename);

			if (code.getFiles() == null) {
				code.setFiles(new HashMap<>());
			}
			code.getFiles().put(filename, codeFile);
			codeService.updateAsset(code);

			// 3. create model from code asset
			return postCodeToAMR(createdCode.getId(), "temp model", "temp model description", false, false);
		} catch (final IOException e) {
			log.error("Unable to upload file", e);
			throw new ResponseStatusException(
					org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
					"Error creating running code to model: " + e.getMessage());
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
			@RequestParam(value = "document-id", required = false) final UUID documentId) {

		try {
			final Provenance provenancePayload = new Provenance(ProvenanceRelationType.EXTRACTED_FROM, modelId,
					ProvenanceType.MODEL, documentId, ProvenanceType.DOCUMENT);
			provenanceService.createProvenance(provenancePayload);
		} catch (final Exception e) {
			final String error = "Unable to create provenance for profile-model";
			log.error(error, e);
		}

		try {
			final Optional<DocumentAsset> documentOptional = documentService.getAsset(documentId);
			String documentText = "";
			if (documentOptional.isPresent()) {
				final int MAX_CHAR_LIMIT = 9000;

				final DocumentAsset document = documentOptional.get();
				documentText = document.getText().substring(0, Math.min(document.getText().length(), MAX_CHAR_LIMIT));
			}

			final Model model = modelService.getAsset(modelId).orElseThrow();

			final StringMultipartFile textFile = new StringMultipartFile(documentText, "document.txt",
					"application/text");
			final StringMultipartFile codeFile = new StringMultipartFile("", "code.txt",
					"application/text");

			final ResponseEntity<JsonNode> resp;
			try {
				resp = mitProxy.modelCard(MIT_OPENAI_API_KEY, textFile, codeFile);
			} catch (final FeignException e) {
				final String error = "Unable to get model card";
				log.error(error, e);
				throw new ResponseStatusException(
						e.status() < 100 ? HttpStatus.INTERNAL_SERVER_ERROR : HttpStatus.valueOf(e.status()),
						error + ": " + e.getMessage());
			}

			if (!resp.getStatusCode().is2xxSuccessful()) {
				throw new ResponseStatusException(
						resp.getStatusCode(),
						"Unable to get model card");
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

		} catch (final IOException e) {
			log.error("Unable to get profile model", e);
			throw new ResponseStatusException(
					org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
					"Unable to get profile model: " + e.getMessage());
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
						"", "application/text");
			}

			final Dataset dataset = datasetService.getAsset(datasetId).orElseThrow();

			if (dataset.getFileNames() == null || dataset.getFileNames().isEmpty()) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No files found on dataset");
			}
			final String filename = dataset.getFileNames().get(0);

			final String csvContents = datasetService.fetchFileAsString(datasetId, filename).orElseThrow();

			final StringMultipartFile csvFile = new StringMultipartFile(csvContents, filename, "application/csv");

			final ResponseEntity<JsonNode> resp = mitProxy.dataCard(MIT_OPENAI_API_KEY, csvFile, documentFile);
			if (!resp.getStatusCode().is2xxSuccessful()) {
				throw new ResponseStatusException(
						resp.getStatusCode(),
						"Unable to get data card");
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

			// add card to metadata
			if (dataset.getMetadata() == null) {
				dataset.setMetadata(mapper.createObjectNode());
			}
			((ObjectNode) dataset.getMetadata()).set("dataCard", card);

			return ResponseEntity.ok(datasetService.updateAsset(dataset).orElseThrow());

		} catch (final FeignException e) {
			final String error = "Unable to get profile dataset";
			log.error(error, e);
			throw new ResponseStatusException(
					e.status() < 100 ? org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
							: HttpStatus.valueOf(e.status()),
					error + ": " + e.getMessage());
		} catch (final Exception e) {
			final String error = "Unable to get profile dataset";
			log.error(error, e);
			throw new ResponseStatusException(
					org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
					error + ": " + e.getMessage());
		}
	}

	@PostMapping("/align-model")
	@Secured(Roles.USER)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "204", description = "Model as been align with document", content = @Content),
			@ApiResponse(responseCode = "500", description = "Error aligning model with variable extracted from document", content = @Content)
	})
	public ResponseEntity<Model> alignModel(
			@RequestParam("document-id") final UUID documentId,
			@RequestParam("model-id") final UUID modelId) {

		try {
			return ResponseEntity.ok(extractionService.alignAMR(documentId, modelId).get());
		} catch (final InterruptedException | ExecutionException e) {
			log.error("Error aligning model with document", e);
			throw new ResponseStatusException(
					org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
					"Error aligning model with document: " + e.getMessage());
		}
	}

	/**
	 * Variables Extractions from Document with SKEMA
	 *
	 * @param documentId (String): The ID of the document to profile
	 * @param modelIds   (List<String>): The IDs of the models to use for extraction
	 * @param domain     (String): The domain of the document
	 * @return an accepted response, the request being handled asynchronously
	 */
	@PostMapping("/variable-extractions")
	public ResponseEntity<Void> variableExtractions(
			@RequestParam("document-id") final UUID documentId,
			@RequestParam(name = "model-ids", required = false) final List<UUID> modelIds,
			@RequestParam(name = "domain", defaultValue = "epi") final String domain) {
		extractionService.extractVariables(documentId, modelIds == null ? new ArrayList<>() : modelIds, domain);
		return ResponseEntity.accepted().build();
	}

	/**
	 * Document Extractions
	 *
	 * @param documentId (String): The ID of the document to profile
	 * @return an accepted response, the request being handled asynchronously
	 */
	@PostMapping("/pdf-extractions")
	@Secured(Roles.USER)
	@Operation(summary = "Extracts information from the first PDF associated with the given document id")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "202", description = "Extraction started on PDF", content = @Content),
			@ApiResponse(responseCode = "500", description = "Error running PDF extraction", content = @Content)
	})
	public ResponseEntity<Void> pdfExtractions(
			@RequestParam("document-id") final UUID documentId,
			@RequestParam(name = "domain", defaultValue = "epi") final String domain) {
		extractionService.extractPDF(documentId, domain);
		return ResponseEntity.accepted().build();
	}

}
