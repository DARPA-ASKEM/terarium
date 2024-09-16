package software.uncharted.terarium.hmiserver.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import feign.FeignException;
import jakarta.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.entity.ContentType;
import org.redisson.api.RMapCache;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import software.uncharted.terarium.hmiserver.models.ClientEventType;
import software.uncharted.terarium.hmiserver.models.TerariumAssetEmbeddings;
import software.uncharted.terarium.hmiserver.models.dataservice.document.DocumentAsset;
import software.uncharted.terarium.hmiserver.models.dataservice.document.DocumentExtraction;
import software.uncharted.terarium.hmiserver.models.dataservice.document.ExtractionAssetType;
import software.uncharted.terarium.hmiserver.models.dataservice.model.Model;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.ModelMetadata;
import software.uncharted.terarium.hmiserver.models.dataservice.provenance.Provenance;
import software.uncharted.terarium.hmiserver.models.dataservice.provenance.ProvenanceRelationType;
import software.uncharted.terarium.hmiserver.models.dataservice.provenance.ProvenanceType;
import software.uncharted.terarium.hmiserver.models.task.TaskRequest;
import software.uncharted.terarium.hmiserver.models.task.TaskRequest.TaskType;
import software.uncharted.terarium.hmiserver.models.task.TaskResponse;
import software.uncharted.terarium.hmiserver.proxies.documentservice.ExtractionProxy;
import software.uncharted.terarium.hmiserver.proxies.skema.SkemaUnifiedProxy;
import software.uncharted.terarium.hmiserver.proxies.skema.SkemaUnifiedProxy.IntegratedTextExtractionsBody;
import software.uncharted.terarium.hmiserver.service.data.DocumentAssetService;
import software.uncharted.terarium.hmiserver.service.data.ModelService;
import software.uncharted.terarium.hmiserver.service.data.ProvenanceService;
import software.uncharted.terarium.hmiserver.service.gollm.EmbeddingService;
import software.uncharted.terarium.hmiserver.service.notification.NotificationGroupInstance;
import software.uncharted.terarium.hmiserver.service.notification.NotificationService;
import software.uncharted.terarium.hmiserver.service.tasks.ExtractEquationsResponseHandler;
import software.uncharted.terarium.hmiserver.service.tasks.ModelCardResponseHandler;
import software.uncharted.terarium.hmiserver.service.tasks.TaskService;
import software.uncharted.terarium.hmiserver.utils.ByteMultipartFile;
import software.uncharted.terarium.hmiserver.utils.JsonUtil;
import software.uncharted.terarium.hmiserver.utils.StringMultipartFile;
import software.uncharted.terarium.hmiserver.utils.rebac.Schema;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExtractionService {

	private final DocumentAssetService documentService;
	private final ModelService modelService;
	private final ExtractionProxy extractionProxy;
	private final SkemaUnifiedProxy skemaUnifiedProxy;
	private final ObjectMapper objectMapper;
	private final ClientEventService clientEventService;
	private final NotificationService notificationService;
	private final TaskService taskService;
	private final ProvenanceService provenanceService;
	private final EmbeddingService embeddingService;
	private final CurrentUserService currentUserService;

	private static final String RESPONSE_CACHE_KEY = "extraction-service-response-cache";

	// TTL = Time to live, the maximum time a key will be in the cache before it is
	// evicted, regardless of activity.
	@Value("${terarium.extractionService.response-cache-ttl-seconds:604800}") // 7 days
	private long CACHE_TTL_SECONDS;

	// Max idle = The maximum time a key can be idle in the cache before it is
	// evicted.
	@Value("${terarium.extractionService.response-cache-max-idle-seconds:86400}") // 24 hours
	private long CACHE_MAX_IDLE_SECONDS;

	private final RedissonClient redissonClient;

	private RMapCache<String, ExtractPDFResponse> responseCache;

	// Used to get the Abstract text from PDF
	private static final String NODE_CONTENT = "content";

	// time the progress takes to reach each subsequent half.
	final Double HALFTIME_SECONDS = 2.0;

	@Value("${terarium.extractionService.poolSize:10}")
	private int POOL_SIZE;

	@Value("${openai-api-key:}")
	String OPENAI_API_KEY;

	private ExecutorService executor;
	private final Environment env;

	private boolean isRunningLocalProfile() {
		final String[] activeProfiles = env.getActiveProfiles();
		for (final String profile : activeProfiles) {
			if ("local".equals(profile)) {
				return true;
			}
		}
		return false;
	}

	@PostConstruct
	void init() {
		executor = Executors.newFixedThreadPool(POOL_SIZE);

		// use a distributed cache and lock so that these can be synchronized across
		// multiple instances of the hmi-server
		responseCache = redissonClient.getMapCache(RESPONSE_CACHE_KEY);

		if (isRunningLocalProfile()) {
			// sanity check for local development to clear the caches
			responseCache.clear();
		}
	}

	@Data
	private static class Properties {

		private final UUID documentId;
	}

	public static String removeFileExtension(final String filename) {
		final int lastIndexOfDot = filename.lastIndexOf(".");
		if (lastIndexOfDot != -1) {
			return filename.substring(0, lastIndexOfDot);
		}
		return filename;
	}

	static class ExtractionFile {

		ExtractionFile(final String filename, final byte[] bytes, final ContentType contentType) {
			this.filename = filename;
			this.bytes = bytes;
			this.contentType = contentType;
		}

		ContentType contentType;
		String filename;
		byte[] bytes;
	}

	static class ExtractPDFResponse {

		JsonNode abstractJsonNode;
		String documentAbstract;
		String documentText;
		List<ExtractionFile> files = new ArrayList<>();
		List<DocumentExtraction> assets = new ArrayList<>();
		ArrayNode variableAttributes;
		JsonNode gollmCard;
	}

	public ExtractPDFResponse runExtractPDF(
		final String documentName,
		final byte[] documentContents,
		final String userId,
		final NotificationGroupInstance<Properties> notificationInterface
	) {
		final ExtractPDFResponse extractionResponse = new ExtractPDFResponse();

		try {
			notificationInterface.sendMessage("Starting extraction...");

			final ByteMultipartFile documentFile = new ByteMultipartFile(documentContents, documentName, "application/pdf");

			final boolean compressImages = false;
			final boolean useCache = false;
			final ResponseEntity<JsonNode> extractionResp = extractionProxy.processPdfExtraction(
				compressImages,
				useCache,
				documentFile
			);

			final JsonNode body = extractionResp.getBody();
			final UUID jobId = UUID.fromString(body.get("job_id").asText());

			final int POLLING_INTERVAL_SECONDS = 5;
			final int MAX_EXECUTION_TIME_SECONDS = 600;
			final int MAX_ITERATIONS = MAX_EXECUTION_TIME_SECONDS / POLLING_INTERVAL_SECONDS;

			boolean jobDone = false;
			notificationInterface.sendMessage("COSMOS extraction in progress...");

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
					notificationInterface.sendMessage("COSMOS extraction complete; processing results...");
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

			notificationInterface.sendMessage("Uploading COSMOS extraction results...");
			final String zipFileName = documentName + "_cosmos.zip";

			extractionResponse.files.add(
				new ExtractionFile(zipFileName, zipFileResp.getBody(), ContentType.APPLICATION_OCTET_STREAM)
			);

			JsonNode abstractJsonNode = null;
			// Open the zipfile and extract the contents
			notificationInterface.sendMessage("Extracting COSMOS extraction results...");
			final Map<String, byte[]> fileMap = new HashMap<>();
			try {
				final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(zipFileResp.getBody());
				final ZipInputStream zipInputStream = new ZipInputStream(byteArrayInputStream);

				ZipEntry entry = zipInputStream.getNextEntry();
				while (entry != null) {
					log.info("Adding {} to filemap", entry.getName());
					final String filenameNoExt = removeFileExtension(entry.getName());
					final byte[] bytes = zipEntryToBytes(zipInputStream);

					fileMap.put(filenameNoExt, bytes);
					if (entry != null && entry.getName().toLowerCase().endsWith(".json")) {
						final ObjectMapper objectMapper = new ObjectMapper();

						final JsonNode rootNode = objectMapper.readTree(bytes);
						if (rootNode instanceof ArrayNode) {
							final ArrayNode arrayNode = (ArrayNode) rootNode;
							for (final JsonNode record : arrayNode) {
								if (record.has("detect_cls") && record.get("detect_cls").asText().equals("Abstract")) {
									abstractJsonNode = record;
									break;
								}
							}
						}
					}
					entry = zipInputStream.getNextEntry();
				}

				zipInputStream.closeEntry();
				zipInputStream.close();
			} catch (final IOException e) {
				throw new RuntimeException("Unable to extract the contents of the zip file", e);
			}

			if (abstractJsonNode != null) {
				extractionResponse.documentAbstract = abstractJsonNode.get(NODE_CONTENT).asText();
			}

			final ResponseEntity<JsonNode> textResp = extractionProxy.text(jobId);
			if (!textResp.getStatusCode().is2xxSuccessful()) {
				throw new RuntimeException("Unable to fetch the text extractions");
			}

			for (final ExtractionAssetType extractionType : ExtractionAssetType.values()) {
				final ResponseEntity<JsonNode> response = extractionProxy.extraction(jobId, extractionType.toStringPlural());
				log.info("Extraction type {} response status: {}", extractionType, response.getStatusCode());
				if (!response.getStatusCode().is2xxSuccessful()) {
					log.warn("Unable to fetch the {} extractions", extractionType);
					continue;
				}

				for (final JsonNode record : response.getBody()) {
					String assetFileName = "";
					if (record.has("img_pth")) {
						final String path = record.get("img_pth").asText();
						assetFileName = path.substring(path.lastIndexOf("/") + 1);
						final String assetFilenameNoExt = removeFileExtension(assetFileName);
						if (!fileMap.containsKey(assetFilenameNoExt)) {
							log.warn("Unable to find file {} in zipfile", assetFileName);
						}
						final byte[] file = fileMap.get(assetFilenameNoExt);
						if (file == null) {
							throw new RuntimeException("Unable to find file " + assetFileName + " in zipfile");
						}
						extractionResponse.files.add(new ExtractionFile(assetFileName, file, ContentType.IMAGE_JPEG));
					} else {
						log.warn("No img_pth found in record: {}", record);
					}

					final DocumentExtraction extraction = new DocumentExtraction();
					extraction.setFileName(assetFileName);
					extraction.setAssetType(extractionType);
					extraction.setMetadata(objectMapper.convertValue(record, new TypeReference<>() {}));

					extractionResponse.assets.add(extraction);
					notificationInterface.sendMessage(String.format("Add COSMOS extraction %s to Document...", assetFileName));
				}
			}

			String documentText = "";
			for (final JsonNode record : textResp.getBody()) {
				if (record.has(NODE_CONTENT)) {
					documentText += record.get(NODE_CONTENT).asText() + "\n";
				} else {
					log.warn("No content found in record: {}", record);
				}
			}
			extractionResponse.documentText = documentText;

			// if there is text, run variable extraction
			if (!documentText.isEmpty()) {
				// run variable extraction
				try {
					notificationInterface.sendMessage("Dispatching variable extraction request...");
					extractionResponse.variableAttributes = getVariablesFromDocumentText(notificationInterface, documentText);
					notificationInterface.sendMessage("Variable extraction completed");
				} catch (final Exception e) {
					notificationInterface.sendMessage("Variable extraction failed, continuing");
				}

				// check for input length
				if (documentText.length() > ModelCardResponseHandler.MAX_TEXT_SIZE) {
					log.warn("Document {} text too long for GoLLM model card task, not sending request");
				} else {
					// dispatch GoLLM model card request
					final ModelCardResponseHandler.Input input = new ModelCardResponseHandler.Input();
					input.setResearchPaper(documentText);

					// Create the task
					final TaskRequest req = new TaskRequest();
					req.setType(TaskRequest.TaskType.GOLLM);
					req.setScript(ModelCardResponseHandler.NAME);
					req.setInput(objectMapper.writeValueAsBytes(input));
					req.setUserId(userId);

					notificationInterface.sendMessage("Sending GoLLM model card request");

					final TaskResponse resp = taskService.runTaskSync(req);

					extractionResponse.gollmCard = objectMapper
						.readValue(resp.getOutput(), ModelCardResponseHandler.Response.class)
						.getResponse();

					notificationInterface.sendMessage("Model Card created");
				}
			}

			return extractionResponse;
		} catch (final FeignException e) {
			final String error = "Transitive service failure";
			log.error(error, e.contentUTF8(), e);
			throw new ResponseStatusException(
				e.status() < 100 ? HttpStatus.INTERNAL_SERVER_ERROR : HttpStatus.valueOf(e.status()),
				error + ": " + e.getMessage()
			);
		} catch (final RuntimeException e) {
			notificationInterface.sendError(e.getMessage());
			log.error(e.getMessage(), e);
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
		} catch (final Exception e) {
			final String error = "Unable to extract pdf";
			log.error(error, e);
			notificationInterface.sendError("Extraction failed, unexpected error.");
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, error);
		}
	}

	public DocumentAsset applyExtractPDFResponse(
		final UUID documentId,
		final UUID projectId,
		final ExtractPDFResponse extractionResponse,
		final Schema.Permission hasWritePermission
	) throws IOException {
		final DocumentAsset document = documentService.getAsset(documentId, hasWritePermission).get();

		for (final ExtractionFile file : extractionResponse.files) {
			document.getFileNames().add(file.filename);
			documentService.uploadFile(documentId, file.filename, file.contentType, file.bytes);
		}

		document.setAssets(new ArrayList<>());
		for (final DocumentExtraction extraction : extractionResponse.assets) {
			document.getAssets().add(extraction);
		}

		if (extractionResponse.documentText != null) {
			document.setText(extractionResponse.documentText);
		}

		if (extractionResponse.documentAbstract != null) {
			document.setDocumentAbstract(extractionResponse.documentAbstract);
		}

		if (extractionResponse.variableAttributes != null) {
			if (document.getMetadata() == null) {
				document.setMetadata(new HashMap<>());
			}
			document.getMetadata().put("attributes", extractionResponse.variableAttributes);
		}

		if (extractionResponse.gollmCard != null) {
			if (document.getMetadata() == null) {
				document.setMetadata(new HashMap<>());
			}
			document.getMetadata().put("gollmCard", extractionResponse.gollmCard);
		}

		log.info("Added extraction to document: {}", documentId);

		return documentService.updateAsset(document, projectId, hasWritePermission).orElseThrow();
	}

	private static String sha256(final byte[] input) {
		try {
			// Create a MessageDigest instance for SHA-256
			final MessageDigest digest = MessageDigest.getInstance("SHA-256");

			// Convert the input string to bytes and compute the hash
			final byte[] hashBytes = digest.digest(input);

			// Convert the hash bytes to a hexadecimal string
			final StringBuilder hexString = new StringBuilder();
			for (final byte b : hashBytes) {
				final String hex = Integer.toHexString(0xff & b);
				if (hex.length() == 1) hexString.append('0');
				hexString.append(hex);
			}

			return hexString.toString();
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	public Future<DocumentAsset> extractPDFAndApplyToDocument(
		final UUID documentId,
		final UUID projectId,
		final Schema.Permission hasWritePermission
	) {
		final NotificationGroupInstance<Properties> notificationInterface = new NotificationGroupInstance<>(
			clientEventService,
			notificationService,
			ClientEventType.EXTRACTION_PDF,
			projectId,
			new Properties(documentId),
			HALFTIME_SECONDS
		);

		final String userId = currentUserService.get().getId();

		return executor.submit(() -> {
			final DocumentAsset document = documentService.getAsset(documentId, hasWritePermission).get();
			notificationInterface.sendMessage("Document found, fetching file...");

			if (document.getFileNames().isEmpty()) {
				throw new RuntimeException("No files found on document");
			}

			final String filename = document.getFileNames().get(0);

			final byte[] documentContents = documentService.fetchFileAsBytes(documentId, filename).get();

			final String documentSHA = sha256(documentContents);

			log.info("Document SHA: {}, checking cache", documentSHA);

			ExtractPDFResponse extractionResponse = null;
			if (responseCache.containsKey(documentSHA)) {
				log.info("Returning cached response for document {} for SHA: {}", documentId, documentSHA);

				extractionResponse = responseCache.get(documentSHA);
				notificationInterface.sendMessage("Cached response found for document extraction");
			} else {
				log.info("No cached response found for text from document {} for SHA: {}", documentId, documentSHA);

				notificationInterface.sendMessage("No extraction found in cache, dispatching extraction request...");
				extractionResponse = runExtractPDF(filename, documentContents, userId, notificationInterface);
				responseCache.put(
					documentSHA,
					extractionResponse,
					CACHE_TTL_SECONDS,
					TimeUnit.SECONDS,
					CACHE_MAX_IDLE_SECONDS,
					TimeUnit.SECONDS
				);
			}

			notificationInterface.sendMessage("Applying extraction results to document");
			log.info("Applying extraction results to document {}", documentId);
			final DocumentAsset doc = applyExtractPDFResponse(documentId, projectId, extractionResponse, hasWritePermission);

			notificationInterface.sendFinalMessage("Extraction complete");

			return doc;
		});
	}

	private ArrayNode getVariablesFromDocumentText(
		final NotificationGroupInstance<Properties> notificationInterface,
		final String documentText
	) {
		notificationInterface.sendMessage("Starting variable extraction.");
		try {
			notificationInterface.sendMessage("Sending request to be processes by MIT.");

			final IntegratedTextExtractionsBody body = new IntegratedTextExtractionsBody(documentText, new ArrayList<>());

			log.info("Sending variable extraction request to SKEMA using MIT only");
			final ResponseEntity<JsonNode> resp = skemaUnifiedProxy.integratedTextExtractions(true, false, body);

			notificationInterface.sendMessage("Response received.");

			// Create a collection to hold the variable extractions
			JsonNode collection = null;
			if (resp.getStatusCode().is2xxSuccessful()) {
				for (final JsonNode output : resp.getBody().get("outputs")) {
					if (!output.has("errors") || output.get("errors").isEmpty()) {
						collection = output.get("data");
						break;
					}
				}
			} else {
				throw new RuntimeException("non successful response.");
			}

			if (collection == null) {
				throw new RuntimeException("No variables extractions returned");
			}

			notificationInterface.sendMessage("Organizing and saving the extractions.");
			final ArrayNode attributes = objectMapper.createArrayNode();
			for (final JsonNode attribute : collection.get("attributes")) {
				attributes.add(attribute);
			}

			return attributes;
		} catch (final FeignException e) {
			final String error = "Transitive service failure";
			log.error(error, e.contentUTF8(), e);
			throw new ResponseStatusException(
				e.status() < 100 ? HttpStatus.INTERNAL_SERVER_ERROR : HttpStatus.valueOf(e.status()),
				error + ": " + e.getMessage()
			);
		} catch (final Exception e) {
			final String error = "SKEMA unified integrated-text-extractions request failed.";
			log.error(error, e);
			notificationInterface.sendError(error + " — " + e.getMessage());
			throw new ResponseStatusException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, error);
		}
	}

	private DocumentAsset runVariableExtraction(
		final NotificationGroupInstance<Properties> notificationInterface,
		final UUID projectId,
		final UUID documentId,
		final List<UUID> modelIds,
		final Schema.Permission hasWritePermission
	) {
		notificationInterface.sendMessage("Starting variable extraction.");
		try {
			// Fetch the text from the document
			final DocumentAsset document = documentService.getAsset(documentId, hasWritePermission).orElseThrow();
			notificationInterface.sendMessage("Document found, fetching text.");
			if (document.getText() == null || document.getText().isEmpty()) {
				throw new RuntimeException("No text found in paper document");
			}

			// add optional models
			final List<Model> models = new ArrayList<>();
			for (final UUID modelId : modelIds) {
				models.add(modelService.getAsset(modelId, hasWritePermission).orElseThrow());
			}
			notificationInterface.sendMessage("Model(s) found, added to extraction request.");

			// Create a collection to hold the variable extractions
			JsonNode collection = null;

			notificationInterface.sendMessage("Sending request to be processes by MIT.");

			final IntegratedTextExtractionsBody body = new IntegratedTextExtractionsBody(document.getText(), models);

			log.info("Sending variable extraction request to SKEMA using MIT only");
			final ResponseEntity<JsonNode> resp = skemaUnifiedProxy.integratedTextExtractions(true, false, body);

			notificationInterface.sendMessage("Response received.");
			if (resp.getStatusCode().is2xxSuccessful()) {
				for (final JsonNode output : resp.getBody().get("outputs")) {
					if (!output.has("errors") || output.get("errors").isEmpty()) {
						collection = output.get("data");
						break;
					}
				}
			} else {
				throw new RuntimeException("non successful response.");
			}

			if (collection == null) {
				throw new RuntimeException("No variables extractions returned");
			}

			notificationInterface.sendMessage("Organizing and saving the extractions.");
			final ArrayNode attributes = objectMapper.createArrayNode();
			for (final JsonNode attribute : collection.get("attributes")) {
				attributes.add(attribute);
			}

			// add the attributes to the metadata
			if (document.getMetadata() == null) {
				document.setMetadata(new HashMap<>());
			}
			document.getMetadata().put("attributes", attributes);

			if (modelIds.size() > 0) {
				for (final UUID modelId : modelIds) {
					notificationInterface.sendMessage("Attempting to align models for model: " + modelId);
					try {
						runAlignAMR(notificationInterface, projectId, documentId, modelId, hasWritePermission);
						notificationInterface.sendMessage("Model " + modelId + " aligned successfully");
					} catch (final Exception e) {
						notificationInterface.sendMessage("Failed to align model: " + modelId + ", continuing...");
					}
				}
			}

			// update the document
			return documentService.updateAsset(document, projectId, hasWritePermission).orElseThrow();
		} catch (final FeignException e) {
			final String error = "Transitive service failure";
			log.error(error, e.contentUTF8(), e);
			throw new ResponseStatusException(
				e.status() < 100 ? HttpStatus.INTERNAL_SERVER_ERROR : HttpStatus.valueOf(e.status()),
				error + ": " + e.getMessage()
			);
		} catch (final Exception e) {
			final String error =
				"SKEMA unified integrated-text-extractions request from document: " + documentId + " failed.";
			log.error(error, e);
			notificationInterface.sendError(error + " — " + e.getMessage());
			throw new ResponseStatusException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, error);
		}
	}

	public Future<DocumentAsset> extractVariables(
		final UUID projectId,
		final UUID documentId,
		final List<UUID> modelIds,
		final Schema.Permission hasWritePermission
	) {
		// Set up the client interface
		final NotificationGroupInstance<Properties> notificationInterface = new NotificationGroupInstance<>(
			clientEventService,
			notificationService,
			ClientEventType.EXTRACTION,
			null,
			new Properties(documentId),
			HALFTIME_SECONDS
		);
		notificationInterface.sendMessage("Variable extraction task submitted...");

		return executor.submit(() -> {
			final DocumentAsset doc = runVariableExtraction(
				notificationInterface,
				projectId,
				documentId,
				modelIds,
				hasWritePermission
			);
			notificationInterface.sendFinalMessage("Extraction complete");
			return doc;
		});
	}

	public Future<Model> alignAMR(
		final UUID projectId,
		final UUID documentId,
		final UUID modelId,
		final Schema.Permission hasWritePermission
	) {
		final NotificationGroupInstance<Properties> notificationInterface = new NotificationGroupInstance<>(
			clientEventService,
			notificationService,
			ClientEventType.EXTRACTION,
			null,
			new Properties(documentId),
			HALFTIME_SECONDS
		);

		notificationInterface.sendMessage("Model alignment task submitted...");

		return executor.submit(() -> {
			final Model model = runAlignAMR(notificationInterface, projectId, documentId, modelId, hasWritePermission);
			notificationInterface.sendFinalMessage("Alignment complete");
			return model;
		});
	}

	public Model runAlignAMR(
		final NotificationGroupInstance<Properties> notificationInterface,
		final UUID projectId,
		final UUID documentId,
		final UUID modelId,
		final Schema.Permission hasWritePermission
	) {
		try {
			notificationInterface.sendMessage("Starting model alignment...");

			final DocumentAsset document = documentService.getAsset(documentId, hasWritePermission).orElseThrow();

			final Model model = modelService.getAsset(modelId, hasWritePermission).orElseThrow();

			final String modelString = objectMapper.writeValueAsString(model);

			if (document.getMetadata() == null) {
				document.setMetadata(new HashMap<>());
			}
			if (document.getMetadata().get("attributes") == null) {
				throw new RuntimeException("No attributes found in document");
			}

			final JsonNode attributes = objectMapper.valueToTree(document.getMetadata().get("attributes"));

			final ObjectNode extractions = objectMapper.createObjectNode();
			extractions.set("attributes", attributes);

			final String extractionsString = objectMapper.writeValueAsString(extractions);

			final StringMultipartFile amrFile = new StringMultipartFile(modelString, "amr.json", "application/json");
			final StringMultipartFile extractionFile = new StringMultipartFile(
				extractionsString,
				"extractions.json",
				"application/json"
			);

			final ResponseEntity<JsonNode> res;
			try {
				res = skemaUnifiedProxy.linkAMRFile(amrFile, extractionFile);
			} catch (final FeignException e) {
				final String error = "Transitive service failure";
				log.error(error, e.contentUTF8(), e);
				throw new ResponseStatusException(
					e.status() < 100 ? HttpStatus.INTERNAL_SERVER_ERROR : HttpStatus.valueOf(e.status()),
					error + ": " + e.getMessage()
				);
			}
			if (!res.getStatusCode().is2xxSuccessful()) {
				throw new ResponseStatusException(res.getStatusCode(), "Unable to link AMR file");
			}

			final JsonNode modelJson = objectMapper.valueToTree(model);

			// overwrite all updated fields
			JsonUtil.recursiveSetAll((ObjectNode) modelJson, res.getBody());

			final Model updatedModel = objectMapper.treeToValue(modelJson, Model.class);

			if (updatedModel.getMetadata() == null) {
				updatedModel.setMetadata(new ModelMetadata());
			}

			// add document gollm card to model
			final JsonNode card = document.getMetadata().get("gollmCard");
			if (card != null) {
				updatedModel.getMetadata().setGollmCard(card);
			}

			// update the model
			modelService.updateAsset(model, projectId, hasWritePermission);

			// create provenance
			final Provenance provenance = new Provenance(
				ProvenanceRelationType.EXTRACTED_FROM,
				modelId,
				ProvenanceType.MODEL,
				documentId,
				ProvenanceType.DOCUMENT
			);
			provenanceService.createProvenance(provenance);

			// update model embeddings
			if (card != null && model.getPublicAsset() && !model.getTemporary()) {
				final String cardText = objectMapper.writeValueAsString(card);
				try {
					final TerariumAssetEmbeddings embeddings = embeddingService.generateEmbeddings(cardText);

					modelService.uploadEmbeddings(modelId, embeddings, hasWritePermission);
					notificationInterface.sendMessage("Embeddings created");
				} catch (final Exception e) {
					log.warn("Unable to generate embedding vectors for model");
				}
			}

			return model;
		} catch (final FeignException e) {
			final String error = "Transitive service failure";
			log.error(error, e.contentUTF8(), e);
			throw new ResponseStatusException(
				e.status() < 100 ? HttpStatus.INTERNAL_SERVER_ERROR : HttpStatus.valueOf(e.status()),
				error + ": " + e.getMessage()
			);
		} catch (final Exception e) {
			final String error = "SKEMA link_amr request from document: " + documentId + " failed.";
			log.error(error, e);
			notificationInterface.sendError(error + " — " + e.getMessage());
			throw new ResponseStatusException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, error);
		}
	}

	public static byte[] zipEntryToBytes(final ZipInputStream zipInputStream) throws IOException {
		final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		final byte[] buffer = new byte[1024];
		int len;
		while ((len = zipInputStream.read(buffer)) > 0) {
			byteArrayOutputStream.write(buffer, 0, len);
		}

		final byte[] bytes = byteArrayOutputStream.toByteArray();
		if (bytes.length == 0) {
			throw new IOException("Empty file found in zip");
		}

		return bytes;
	}

	@Value("${terarium.taskrunner.nougat.gpu-endpoint}")
	private String NOUGAT_GPU_ENDPOINT;

	public Future<ExtractEquationsResponseHandler.ResponseOutput> extractEquationsFromPDF(
		final byte[] pdf,
		final String userId
	) throws JsonProcessingException, TimeoutException, InterruptedException, ExecutionException, IOException {
		final int REQUEST_TIMEOUT_MINUTES = 5;

		final ExtractEquationsResponseHandler.Input input = new ExtractEquationsResponseHandler.Input();
		input.setPdf(pdf);

		final URL url = new URL(String.format("{}/ping", NOUGAT_GPU_ENDPOINT));
		final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod("GET");
		final int responseCode = connection.getResponseCode();

		final TaskRequest req = new TaskRequest();
		req.setTimeoutMinutes(REQUEST_TIMEOUT_MINUTES);
		req.setInput(input);
		req.setScript(ExtractEquationsResponseHandler.NAME);
		req.setUserId(userId);

		if (responseCode == HttpURLConnection.HTTP_OK) {
			// use GPU impl
			req.setType(TaskType.NOUGAT_GPU);
		} else {
			// otherwise fallback to CPU impl
			req.setType(TaskType.NOUGAT_CPU);
		}

		return executor.submit(() -> {
			final TaskResponse resp = taskService.runTaskSync(req);

			final byte[] outputBytes = resp.getOutput();
			return objectMapper.readValue(outputBytes, ExtractEquationsResponseHandler.ResponseOutput.class);
		});
	}
}
