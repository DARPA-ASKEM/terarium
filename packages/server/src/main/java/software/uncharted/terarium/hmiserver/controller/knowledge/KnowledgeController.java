package software.uncharted.terarium.hmiserver.controller.knowledge;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import feign.FeignException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import software.uncharted.terarium.hmiserver.models.dataservice.Grounding;
import software.uncharted.terarium.hmiserver.models.dataservice.Identifier;
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
import software.uncharted.terarium.hmiserver.service.CurrentUserService;
import software.uncharted.terarium.hmiserver.service.ExtractionService;
import software.uncharted.terarium.hmiserver.service.data.CodeService;
import software.uncharted.terarium.hmiserver.service.data.DatasetService;
import software.uncharted.terarium.hmiserver.service.data.DocumentAssetService;
import software.uncharted.terarium.hmiserver.service.data.ModelService;
import software.uncharted.terarium.hmiserver.service.data.ProjectService;
import software.uncharted.terarium.hmiserver.service.data.ProvenanceSearchService;
import software.uncharted.terarium.hmiserver.service.data.ProvenanceService;
import software.uncharted.terarium.hmiserver.utils.ByteMultipartFile;
import software.uncharted.terarium.hmiserver.utils.Messages;
import software.uncharted.terarium.hmiserver.utils.StringMultipartFile;
import software.uncharted.terarium.hmiserver.utils.rebac.Schema;

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

	final ProjectService projectService;
	final CurrentUserService currentUserService;

	final Messages messages;

	@Value("${mit-openai-api-key:}")
	String MIT_OPENAI_API_KEY;

	/**
	 * Send the equations to the skema unified service to get the AMR
	 *
	 * @return UUID Model ID, or null if the model was not created or updated
	 */
	@PostMapping("/equations-to-model")
	@Secured(Roles.USER)
	public ResponseEntity<UUID> equationsToModel(
			@RequestBody final JsonNode req,
			@RequestParam(name = "project-id", required = false) final UUID projectId) {
		final Schema.Permission permission =
				projectService.checkPermissionCanWrite(currentUserService.get().getId(), projectId);

		final Model responseAMR;

		// Check if a model ID is supplied and try to extract it
		UUID modelId = null;
		final String modelIdString =
				req.get("modelId") != null ? req.get("modelId").asText() : null;
		if (modelIdString != null) {
			try {
				// Get the model id if it is a valid UUID
				modelId = UUID.fromString(modelIdString);
			} catch (final IllegalArgumentException e) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, messages.get("generic.invalid-uuid"));
			}
		}

		// Get an AMR from Skema Unified Service
		try {
			responseAMR = skemaUnifiedProxy.consolidatedEquationsToAMR(req).getBody();
			if (responseAMR == null) {
				throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, messages.get("skema.bad-equations"));
			}
		} catch (final FeignException e) {
			final String error = "Skema Unified Service did not return a valid AMR based on the provided Equations";
			log.error(error, e);

			throw handleSkemaFeignException(e);
		} catch (final Exception e) {
			final String error = "An unhandled error occurred while processing the AMR from equations.";
			log.error(error, e);
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, messages.get("skema.internal-error"));
		}

		// If no model id is provided, create a new model asset
		if (modelId == null) {
			try {
				final Model model = modelService.createAsset(responseAMR, permission);
				return ResponseEntity.ok(model.getId());
			} catch (final IOException e) {
				log.error("Unable to create a model", e);
				throw new ResponseStatusException(
						HttpStatus.SERVICE_UNAVAILABLE, messages.get("postgres.service-unavailable"));
			}
		}

		// If a model id is provided, update the existing model
		final Optional<Model> model;

		model = modelService.getAsset(modelId, permission);
		if (model.isEmpty()) {
			final String errorMessage = String.format("The model id %s does not exist.", modelId);
			log.error(errorMessage);
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, messages.get("model.not-found"));
		}

		responseAMR.setId(model.get().getId());
		try {
			modelService.updateAsset(responseAMR, permission);
			return ResponseEntity.ok(model.get().getId());
		} catch (final IOException e) {
			log.error("Unable to update the model with id {}.", modelId, e);
			throw new ResponseStatusException(
					HttpStatus.SERVICE_UNAVAILABLE, messages.get("postgres.service-unavailable"));
		}
	}

	@PostMapping("/base64-equations-to-model")
	@Secured(Roles.USER)
	public ResponseEntity<Model> base64EquationsToAMR(@RequestBody final JsonNode req) {
		try {
			return ResponseEntity.ok(skemaUnifiedProxy.base64EquationsToAMR(req).getBody());
		} catch (final FeignException e) {
			final String error = "Error with Skema Unified Service while converting base64 equations to AMR";
			log.error(error, e);

			throw handleSkemaFeignException(e);
		}
	}

	@PostMapping("/base64-equations-to-latex")
	@Secured(Roles.USER)
	public ResponseEntity<String> base64EquationsToLatex(@RequestBody final JsonNode req) {
		try {
			return ResponseEntity.ok(
					skemaUnifiedProxy.base64EquationsToLatex(req).getBody());
		} catch (final FeignException e) {
			final String error = "Error with Skema Unified Service while converting base64 equations to Latex";
			log.error(error, e);

			throw handleSkemaFeignException(e);
		}
	}

	/**
	 * Transform source code to AMR
	 *
	 * @param codeId (String): id of the code artifact model
	 * @param dynamicsOnly (Boolean): whether to only run the amr extraction over specified dynamics from the code
	 *     object in TDS
	 * @param llmAssisted (Boolean): whether amr extraction is llm assisted
	 * @return Model
	 */
	@PostMapping("/code-to-amr")
	@Secured(Roles.USER)
	ResponseEntity<Model> postCodeToAMR(
			@RequestParam("code-id") final UUID codeId,
			@RequestParam(name = "project-id", required = false) final UUID projectId,
			@RequestParam(name = "name", required = false, defaultValue = "") final String name,
			@RequestParam(name = "description", required = false, defaultValue = "") final String description,
			@RequestParam(name = "dynamics-only", required = false, defaultValue = "false") Boolean dynamicsOnly,
			@RequestParam(name = "llm-assisted", required = false, defaultValue = "false") final Boolean llmAssisted) {
		final Schema.Permission permission =
				projectService.checkPermissionCanWrite(currentUserService.get().getId(), projectId);

		final Optional<Code> code = codeService.getAsset(codeId, permission);
		if (code.isEmpty()) {
			log.error("Unable to fetch the requested code asset with codeId: {}", codeId);
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, messages.get("code.not-found"));
		}

		final Map<String, CodeFile> codeFiles = code.get().getFiles();

		final Map<String, String> codeContent = new HashMap<>();

		for (final Entry<String, CodeFile> file : codeFiles.entrySet()) {
			final String filename = file.getKey();
			final CodeFile codeFile = file.getValue();

			final String content;
			try {
				content = codeService.fetchFileAsString(codeId, filename).orElseThrow();
			} catch (final IOException e) {
				log.error("Unable to fetch code as a string", e);
				throw new ResponseStatusException(
						HttpStatus.SERVICE_UNAVAILABLE, messages.get("postgres.service-unavailable"));
			}

			if (dynamicsOnly
					&& codeFile.getDynamics() != null
					&& codeFile.getDynamics().getBlock() != null) {
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

		if (dynamicsOnly) {
			for (final Entry<String, String> entry : codeContent.entrySet()) {
				files.add(entry.getKey());
				blobs.add(entry.getValue());
			}

			resp = skemaUnifiedProxy.snippetsToAMR(files, blobs);

		} else {
			final ByteArrayOutputStream zipBuffer = new ByteArrayOutputStream();
			final ZipOutputStream zipf = new ZipOutputStream(zipBuffer, StandardCharsets.UTF_8);

			try {
				for (final Map.Entry<String, String> entry : codeContent.entrySet()) {
					final String codeName = entry.getKey();
					final String content = entry.getValue();
					final ZipEntry zipEntry = new ZipEntry(codeName);
					zipf.putNextEntry(zipEntry);
					zipf.write(content.getBytes(StandardCharsets.UTF_8));
					zipf.closeEntry();
				}
			} catch (final IOException e) {
				log.error("Unable to write to zip file", e);
				throw new ResponseStatusException(
						HttpStatus.INTERNAL_SERVER_ERROR, messages.get("generic.io-error.write"));
			} finally {
				try {
					zipf.close();
				} catch (final IOException e) {
					log.error("Unable to close zip file", e);
					throw new ResponseStatusException(
							HttpStatus.INTERNAL_SERVER_ERROR, messages.get("generic.io-error.write"));
				}
			}

			final ByteMultipartFile file =
					new ByteMultipartFile(zipBuffer.toByteArray(), "zip_file.zip", "application/zip");

			resp = llmAssisted ? skemaUnifiedProxy.llmCodebaseToAMR(file) : skemaUnifiedProxy.codebaseToAMR(file);
		}

		if (resp.getStatusCode().is4xxClientError()) {
			throw new ResponseStatusException(resp.getStatusCode(), messages.get("skema.bad-code"));
		} else if (resp.getStatusCode() == HttpStatus.SERVICE_UNAVAILABLE) {
			throw new ResponseStatusException(resp.getStatusCode(), messages.get("skema.service-unavailable"));
		} else if (!resp.getStatusCode().is2xxSuccessful()) {
			throw new ResponseStatusException(resp.getStatusCode(), messages.get("skema.internal-error"));
		}

		Model model;
		try {
			model = mapper.treeToValue(resp.getBody(), Model.class);
		} catch (final IOException e) {
			log.error("Unable to convert response to model", e);
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, messages.get("generic.unknown"));
		}

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

		try {
			model = modelService.createAsset(model, permission);
		} catch (final IOException e) {
			log.error("Unable to create model", e);
			throw new ResponseStatusException(
					HttpStatus.SERVICE_UNAVAILABLE, messages.get("postgres.service-unavailable"));
		}

		// update the code
		if (code.get().getMetadata() == null) {
			code.get().setMetadata(new HashMap<>());
		}
		code.get().getMetadata().put("model_id", model.getId().toString());

		try {
			codeService.updateAsset(code.get(), permission);
		} catch (final IOException e) {
			log.error("Unable to update code", e);
			throw new ResponseStatusException(
					HttpStatus.SERVICE_UNAVAILABLE, messages.get("postgres.service-unavailable"));
		}

		// set the provenance
		final Provenance provenancePayload = new Provenance(
				ProvenanceRelationType.EXTRACTED_FROM,
				model.getId(),
				ProvenanceType.MODEL,
				codeId,
				ProvenanceType.CODE);
		provenanceService.createProvenance(provenancePayload);

		return ResponseEntity.ok(model);
	}

	// Create a model from code blocks
	@Operation(summary = "Create a model from code blocks")
	@ApiResponses(
			value = {
				@ApiResponse(
						responseCode = "200",
						description = "Return the extraction job for code to amr",
						content =
								@Content(
										mediaType = "application/json",
										schema =
												@io.swagger.v3.oas.annotations.media.Schema(
														implementation = ExtractionResponse.class))),
				@ApiResponse(
						responseCode = "400",
						description = "Invalid input - code file may be missing name",
						content = @Content),
				@ApiResponse(
						responseCode = "500",
						description = "Error running code blocks to model",
						content = @Content)
			})
	@PostMapping(value = "/code-blocks-to-model", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@Secured(Roles.USER)
	public ResponseEntity<Model> codeBlocksToModel(
			@RequestParam(name = "project-id", required = false) final UUID projectId,
			@RequestPart final Code code,
			@RequestPart("file") final MultipartFile input) {
		final Schema.Permission permission =
				projectService.checkPermissionCanWrite(currentUserService.get().getId(), projectId);

		// 1. create code asset from code blocks
		final Code createdCode;
		try {
			createdCode = codeService.createAsset(code, permission);
		} catch (final IOException e) {
			log.error("Unable to create code asset", e);
			throw new ResponseStatusException(
					HttpStatus.SERVICE_UNAVAILABLE, messages.get("postgres.service-unavailable"));
		}

		// 2. upload file to code asset
		final byte[] fileAsBytes;
		try {
			fileAsBytes = input.getBytes();
		} catch (final IOException e) {
			log.error("Unable to read file", e);
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, messages.get("generic.io-error.read"));
		}
		final HttpEntity fileEntity = new ByteArrayEntity(fileAsBytes, ContentType.TEXT_PLAIN);
		final String filename = input.getOriginalFilename();

		if (filename == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, messages.get("code.filename-needed"));
		}

		try {
			codeService.uploadFile(code.getId(), filename, fileEntity);
		} catch (final IOException e) {
			log.error("Unable to upload file to code asset", e);
			throw new ResponseStatusException(
					HttpStatus.SERVICE_UNAVAILABLE, messages.get("postgres.service-unavailable"));
		}

		// add the code file to the code asset
		final CodeFile codeFile = new CodeFile();
		codeFile.setFileNameAndProgrammingLanguage(filename);

		if (code.getFiles() == null) {
			code.setFiles(new HashMap<>());
		}
		code.getFiles().put(filename, codeFile);

		try {
			codeService.updateAsset(code, permission);
		} catch (final IOException e) {
			log.error("Unable to update code asset", e);
			throw new ResponseStatusException(
					HttpStatus.SERVICE_UNAVAILABLE, messages.get("postgres.service-unavailable"));
		}

		// 3. create model from code asset
		return postCodeToAMR(createdCode.getId(), projectId, "temp model", "temp model description", false, false);
	}

	/**
	 * Profile a model
	 *
	 * @param modelId (String): The ID of the model to profile
	 * @param documentId (String): The text of the document to profile
	 * @return the profiled model
	 */
	@PostMapping("/profile-model/{model-id}")
	@Secured(Roles.USER)
	public ResponseEntity<Model> postProfileModel(
			@PathVariable("model-id") final UUID modelId,
			@RequestParam(name = "project-id", required = false) final UUID projectId,
			@RequestParam(value = "document-id", required = false) final UUID documentId) {

		final Schema.Permission permission =
				projectService.checkPermissionCanWrite(currentUserService.get().getId(), projectId);

		JsonNode gollmCard = null;
		String documentText = "";
		if (documentId != null) {
			final Optional<DocumentAsset> documentOptional = documentService.getAsset(documentId, permission);
			if (documentOptional.isPresent()) {
				final int MAX_CHAR_LIMIT = 9000;

				final DocumentAsset document = documentOptional.get();

				if (document.getText() != null) {
					documentText = document.getText()
							.substring(0, Math.min(document.getText().length(), MAX_CHAR_LIMIT));
				} else {
					throw new ResponseStatusException(
							HttpStatus.BAD_REQUEST, messages.get("document.extraction.not-done"));
				}
				//Try to get TA4 card.
				if (document.getMetadata() != null){
					if (document.getMetadata().get("gollmCard") != null){
						gollmCard = document.getMetadata().get("gollmCard").deepCopy();
					}
				}

				try {
					final Provenance provenancePayload = new Provenance(
							ProvenanceRelationType.EXTRACTED_FROM,
							modelId,
							ProvenanceType.MODEL,
							documentId,
							ProvenanceType.DOCUMENT);
					provenanceService.createProvenance(provenancePayload);
				} catch (final Exception e) {
					final String error = "Unable to create provenance for profile-model";
					log.error(error, e);
				}
			}
		}

		final Model model = modelService.getAsset(modelId, permission).orElseThrow();

		final StringMultipartFile textFile = new StringMultipartFile(documentText, "document.txt", "application/text");
		final StringMultipartFile codeFile = new StringMultipartFile("", "code.txt", "application/text");

		final ResponseEntity<JsonNode> resp;
		try {
			resp = mitProxy.modelCard(MIT_OPENAI_API_KEY, textFile, codeFile);
		} catch (final FeignException e) {
			final String error = "Unable to get model card";
			log.error(error, e);
			throw handleMitFeignException(e);
		}

		if (resp.getStatusCode().is4xxClientError()) {
			throw new ResponseStatusException(resp.getStatusCode(), messages.get("mit.file.unable-to-read"));
		} else if (resp.getStatusCode() == HttpStatus.SERVICE_UNAVAILABLE) {
			throw new ResponseStatusException(resp.getStatusCode(), messages.get("mit.service-unavailable"));
		} else if (!resp.getStatusCode().is2xxSuccessful()) {
			throw new ResponseStatusException(resp.getStatusCode(), messages.get("mit.internal-error"));
		}

		//Get TA1 Card:
		final Card card;
		try {
			card = mapper.treeToValue(resp.getBody(), Card.class);
		} catch (final IOException e) {
			log.error("Unable to convert response to card", e);
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, messages.get("generic.io-error.write"));
		}

		if (model.getHeader() == null) {
			model.setHeader(new ModelHeader());
		}

		if (model.getMetadata() == null) {
			model.setMetadata(new ModelMetadata());
		}

		model.getHeader().setDescription(card.getDescription());
		model.getMetadata().setCard(card);
		model.getMetadata().setGollmCard(gollmCard);

		final Optional<Model> updatedModel;
		try {
			updatedModel = modelService.updateAsset(model, permission);
		} catch (final IOException e) {
			log.error("Unable to update model", e);
			throw new ResponseStatusException(
					HttpStatus.SERVICE_UNAVAILABLE, messages.get("postgres.service-unavailable"));
		}

		if (updatedModel.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, messages.get("model.unable-to-update"));
		}

		return ResponseEntity.ok(updatedModel.get());
	}

	/**
	 * Profile a dataset
	 *
	 * @param datasetId (String): The ID of the dataset to profile
	 * @param documentId (String): The ID of the document to profile
	 * @return the profiled dataset
	 */
	@PostMapping("/profile-dataset/{dataset-id}")
	@Secured(Roles.USER)
	public ResponseEntity<Dataset> postProfileDataset(
			@PathVariable("dataset-id") final UUID datasetId,
			@RequestParam(name = "project-id", required = false) final UUID projectId,
			@RequestParam(name = "document-id", required = false) final Optional<UUID> documentId) {
		final Schema.Permission permission =
				projectService.checkPermissionCanWrite(currentUserService.get().getId(), projectId);

		// Provenance call if a document id is provided
		StringMultipartFile documentFile = null;
		if (documentId.isPresent()) {

			final DocumentAsset document =
					documentService.getAsset(documentId.get(), permission).orElseThrow();
			documentFile = new StringMultipartFile(document.getText(), documentId.get() + ".txt", "application/text");

			try {
				final Provenance provenancePayload = new Provenance(
						ProvenanceRelationType.EXTRACTED_FROM,
						datasetId,
						ProvenanceType.DATASET,
						documentId.get(),
						ProvenanceType.DOCUMENT);
				provenanceService.createProvenance(provenancePayload);

			} catch (final Exception e) {
				final String error = "Unable to create provenance for profile-dataset";
				log.error(error, e);
			}
		} else {
			documentFile = new StringMultipartFile(
					"There is no documentation for this dataset", "document.txt", "application/text");
		}

		final Dataset dataset = datasetService.getAsset(datasetId, permission).orElseThrow();

		if (dataset.getFileNames() == null || dataset.getFileNames().isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, messages.get("dataset.files.not-found"));
		}
		final String filename = dataset.getFileNames().get(0);

		final String csvContents;
		try {
			csvContents = datasetService.fetchFileAsString(datasetId, filename).orElseThrow();
		} catch (final IOException e) {
			log.error("Unable to fetch file as string", e);
			throw new ResponseStatusException(
					HttpStatus.SERVICE_UNAVAILABLE, messages.get("postgres.service-unavailable"));
		}

		final StringMultipartFile csvFile = new StringMultipartFile(csvContents, filename, "application/csv");

		final ResponseEntity<JsonNode> resp = mitProxy.dataCard(MIT_OPENAI_API_KEY, csvFile, documentFile);

		if (resp.getStatusCode().is4xxClientError()) {
			throw new ResponseStatusException(resp.getStatusCode(), messages.get("mit.file.unable-to-read"));
		} else if (resp.getStatusCode() == HttpStatus.SERVICE_UNAVAILABLE) {
			throw new ResponseStatusException(resp.getStatusCode(), messages.get("mit.service-unavailable"));
		} else if (!resp.getStatusCode().is2xxSuccessful()) {
			throw new ResponseStatusException(resp.getStatusCode(), messages.get("mit.internal-error"));
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
					groundings.setIdentifiers(new ArrayList<>());
				}
				groundings
						.getIdentifiers()
						.add(new Identifier(g.get(0).asText(), g.get(1).asText()));
			}

			// remove groundings from annotation object
			((ObjectNode) annotation).remove("dkg_groundings");

			final DatasetColumn newCol = new DatasetColumn();
			newCol.setName(col.getName());
			newCol.setDataType(col.getDataType());
			newCol.setFormatStr(col.getFormatStr());
			newCol.setGrounding(groundings);
			newCol.setAnnotations(col.getAnnotations());
			newCol.setDescription(annotation.get("description").asText());
			newCol.setMetadata(col.getMetadata());
			newCol.updateMetadata(annotation);
			columns.add(newCol);
		}

		dataset.setColumns(columns);

		// add card to metadata
		if (dataset.getMetadata() == null) {
			dataset.setMetadata(mapper.createObjectNode());
		}
		((ObjectNode) dataset.getMetadata()).set("dataCard", card);

		final Optional<Dataset> updatedDataset;
		try {
			updatedDataset = datasetService.updateAsset(dataset, permission);
		} catch (final IOException e) {
			log.error("Unable to update dataset", e);
			throw new ResponseStatusException(
					HttpStatus.SERVICE_UNAVAILABLE, messages.get("postgres.service-unavailable"));
		}

		if (updatedDataset.isEmpty()) {
			throw new ResponseStatusException(
					HttpStatus.INTERNAL_SERVER_ERROR, messages.get("dataset.unable-to-update"));
		}

		return ResponseEntity.ok(updatedDataset.get());
	}

	@PostMapping("/align-model")
	@Secured(Roles.USER)
	@ApiResponses(
			value = {
				@ApiResponse(
						responseCode = "204",
						description = "Model as been align with document",
						content = @Content),
				@ApiResponse(
						responseCode = "500",
						description = "Error aligning model with variable extracted from document",
						content = @Content)
			})
	public ResponseEntity<Model> alignModel(
			@RequestParam("document-id") final UUID documentId,
			@RequestParam("model-id") final UUID modelId,
			@RequestParam(name = "project-id", required = false) final UUID projectId) {
		final Schema.Permission permission =
				projectService.checkPermissionCanWrite(currentUserService.get().getId(), projectId);

		try {
			return ResponseEntity.ok(
					extractionService.alignAMR(documentId, modelId, permission).get());
		} catch (final InterruptedException | ExecutionException e) {
			log.error("Error aligning model with document", e);
			throw new ResponseStatusException(
					org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, messages.get("skema.error.align-model"));
		}
	}

	/**
	 * Variables Extractions from Document with SKEMA
	 *
	 * @param documentId (String): The ID of the document to profile
	 * @param modelIds (List<String>): The IDs of the models to use for extraction
	 * @param domain (String): The domain of the document
	 * @return an accepted response, the request being handled asynchronously
	 */
	@PostMapping("/variable-extractions")
	public ResponseEntity<Void> variableExtractions(
			@RequestParam("document-id") final UUID documentId,
			@RequestParam(name = "model-ids", required = false) final List<UUID> modelIds,
			@RequestParam(name = "domain", defaultValue = "epi") final String domain,
			@RequestParam(name = "project-id", required = false) final UUID projectId) {
		final Schema.Permission permission =
				projectService.checkPermissionCanWrite(currentUserService.get().getId(), projectId);
		extractionService.extractVariables(
				documentId, modelIds == null ? new ArrayList<>() : modelIds, domain, permission);
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
	@ApiResponses(
			value = {
				@ApiResponse(responseCode = "202", description = "Extraction started on PDF", content = @Content),
				@ApiResponse(responseCode = "500", description = "Error running PDF extraction", content = @Content)
			})
	public ResponseEntity<Void> pdfExtractions(
			@RequestParam("document-id") final UUID documentId,
			@RequestParam(name = "domain", defaultValue = "epi") final String domain,
			@RequestParam(name = "project-id", required = false) final UUID projectId) {
		final Schema.Permission permission =
				projectService.checkPermissionCanWrite(currentUserService.get().getId(), projectId);
		extractionService.extractPDF(documentId, domain, projectId, permission);
		return ResponseEntity.accepted().build();
	}

	private ResponseStatusException handleSkemaFeignException(final FeignException e) {
		final HttpStatus statusCode = HttpStatus.resolve(e.status());
		if (statusCode != null && statusCode.is4xxClientError()) {
			return new ResponseStatusException(statusCode, messages.get("skema.bad-equations"));
		} else if (statusCode == HttpStatus.SERVICE_UNAVAILABLE) {
			return new ResponseStatusException(statusCode, messages.get("skema.service-unavailable"));
		} else if (statusCode != null && statusCode.is5xxServerError()) {
			return new ResponseStatusException(statusCode, messages.get("skema.internal-error"));
		}
		return new ResponseStatusException(statusCode, messages.get("generic.unknown"));
	}

	private ResponseStatusException handleMitFeignException(final FeignException e) {
		final HttpStatus statusCode = HttpStatus.resolve(e.status());
		if (statusCode != null && statusCode.is4xxClientError()) {
			return new ResponseStatusException(statusCode, messages.get("mit.file.unable-to-read"));
		} else if (statusCode == HttpStatus.SERVICE_UNAVAILABLE) {
			return new ResponseStatusException(statusCode, messages.get("mit.service-unavailable"));
		} else if (statusCode != null && statusCode.is5xxServerError()) {
			return new ResponseStatusException(statusCode, messages.get("skema.internal-error"));
		}
		return new ResponseStatusException(statusCode, messages.get("generic.unknown"));
	}
}
