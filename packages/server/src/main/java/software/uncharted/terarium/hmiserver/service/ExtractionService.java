package software.uncharted.terarium.hmiserver.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.http.HttpEntity;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.uncharted.terarium.hmiserver.models.ClientEvent;
import software.uncharted.terarium.hmiserver.models.ClientEventType;
import software.uncharted.terarium.hmiserver.models.dataservice.document.DocumentAsset;
import software.uncharted.terarium.hmiserver.models.dataservice.document.DocumentExtraction;
import software.uncharted.terarium.hmiserver.models.dataservice.document.ExtractionAssetType;
import software.uncharted.terarium.hmiserver.models.dataservice.model.Model;
import software.uncharted.terarium.hmiserver.models.dataservice.provenance.Provenance;
import software.uncharted.terarium.hmiserver.models.dataservice.provenance.ProvenanceRelationType;
import software.uncharted.terarium.hmiserver.models.dataservice.provenance.ProvenanceType;
import software.uncharted.terarium.hmiserver.models.extractionservice.ExtractionStatusUpdate;
import software.uncharted.terarium.hmiserver.models.task.TaskRequest;
import software.uncharted.terarium.hmiserver.models.task.TaskResponse;
import software.uncharted.terarium.hmiserver.models.task.TaskStatus;
import software.uncharted.terarium.hmiserver.proxies.documentservice.ExtractionProxy;
import software.uncharted.terarium.hmiserver.proxies.mit.MitProxy;
import software.uncharted.terarium.hmiserver.proxies.skema.SkemaUnifiedProxy;
import software.uncharted.terarium.hmiserver.proxies.skema.SkemaUnifiedProxy.IntegratedTextExtractionsBody;
import software.uncharted.terarium.hmiserver.service.data.DocumentAssetService;
import software.uncharted.terarium.hmiserver.service.data.ModelService;
import software.uncharted.terarium.hmiserver.service.data.ProvenanceService;
import software.uncharted.terarium.hmiserver.service.tasks.ModelCardResponseHandler;
import software.uncharted.terarium.hmiserver.service.tasks.TaskService;
import software.uncharted.terarium.hmiserver.utils.ByteMultipartFile;
import software.uncharted.terarium.hmiserver.utils.JsonUtil;
import software.uncharted.terarium.hmiserver.utils.StringMultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExtractionService {
	private final DocumentAssetService documentService;
	private final ModelService modelService;
	private final ExtractionProxy extractionProxy;
	private final SkemaUnifiedProxy skemaUnifiedProxy;
	private final MitProxy mitProxy;
	private final ObjectMapper objectMapper;
	private final ClientEventService clientEventService;
	private final TaskService taskService;
	private final ProvenanceService provenanceService;
	private final CurrentUserService currentUserService;

	// time the progress takes to reach each subsequent half
	final Double HALFTIME_SECONDS = 2.0;

	@Value("${mit-openai-api-key:}")
	String MIT_OPENAI_API_KEY;

	private final ExecutorService executor = Executors.newFixedThreadPool(1);

	private static class ClientEventInterface {

		private Double halfTimeSeconds = 2.0;
		private Double startSeconds = 0.0;
		private final UUID documentId;
		private final String userId;
		final ClientEventService clientEventService;

		ClientEventInterface(final ClientEventService clientEventService, final UUID documentId, final String userId,
				final Double halfTimeSeconds) {
			this.clientEventService = clientEventService;
			this.documentId = documentId;
			this.userId = userId;
			this.halfTimeSeconds = halfTimeSeconds;
			this.startSeconds = System.currentTimeMillis() / 1000.0;
		}

		private Double estimateT() {
			return 1.0f - Math.pow(0.5, (getElapsedSeconds() / halfTimeSeconds));
		}

		private Double getElapsedSeconds() {
			return (System.currentTimeMillis() / 1000.0) - startSeconds;
		}

		private void updateClient(final UUID documentId, final Double t, final String message, final String error,
				final String userId) {

			log.info("t: {}, {}", t, message);

			final ExtractionStatusUpdate update = new ExtractionStatusUpdate(documentId, t, message, error);
			final ClientEvent<ExtractionStatusUpdate> status = ClientEvent.<ExtractionStatusUpdate>builder()
					.type(ClientEventType.EXTRACTION).data(update).build();
			clientEventService.sendToUser(status, userId);
		}

		public void sendMessage(final String msg) {
			updateClient(documentId, estimateT(), msg, null, userId);
		}

		public void sendFinalMessage(final String msg) {
			updateClient(documentId, 1.0, msg, null, userId);
		}

		public void sendError(final String msg) {
			updateClient(documentId, estimateT(), null, msg, userId);
		}

	}

	public static String removeFileExtension(final String filename) {
		final int lastIndexOfDot = filename.lastIndexOf(".");
		if (lastIndexOfDot != -1) {
			return filename.substring(0, lastIndexOfDot);
		}
		return filename;
	}

	public Future<DocumentAsset> extractPDF(final UUID documentId, final String domain) {

		final String userId = currentUserService.get().getId();
		final ClientEventInterface clientInterface = new ClientEventInterface(clientEventService, documentId, userId,
				HALFTIME_SECONDS);

		return executor.submit(() -> {
			try {
				clientInterface.sendMessage("Starting extraction...");

				DocumentAsset document = documentService.getAsset(documentId).get();
				clientInterface.sendMessage("Document found, fetching file...");

				if (document.getFileNames().isEmpty()) {
					final String errorMsg = "No files found on document";
					clientInterface.sendError(errorMsg);
					throw new RuntimeException(errorMsg);
				}

				final String filename = document.getFileNames().get(0);

				final byte[] documentContents = documentService.fetchFileAsBytes(documentId, filename).get();
				clientInterface.sendMessage("File fetched, processing PDF extraction...");

				final ByteMultipartFile documentFile = new ByteMultipartFile(documentContents, filename,
						"application/pdf");

				final boolean compressImages = false;
				final boolean useCache = false;
				final ResponseEntity<JsonNode> extractionResp = extractionProxy.processPdfExtraction(compressImages,
						useCache, documentFile);

				final JsonNode body = extractionResp.getBody();
				final UUID jobId = UUID.fromString(body.get("job_id").asText());

				final int POLLING_INTERVAL_SECONDS = 5;
				final int MAX_EXECUTION_TIME_SECONDS = 600;
				final int MAX_ITERATIONS = MAX_EXECUTION_TIME_SECONDS / POLLING_INTERVAL_SECONDS;

				boolean jobDone = false;
				clientInterface.sendMessage("COSMOS extraction in progress...");

				for (int i = 0; i < MAX_ITERATIONS; i++) {

					final ResponseEntity<JsonNode> statusResp = extractionProxy.status(jobId);
					if (!statusResp.getStatusCode().is2xxSuccessful()) {
						final String errorMsg = "Unable to poll status endpoint";
						clientInterface.sendError(errorMsg);
						throw new RuntimeException(errorMsg);
					}

					final JsonNode statusData = statusResp.getBody();
					if (!statusData.get("error").isNull()) {
						final String errorMsg = "Extraction job failed: " + statusData.has("error");
						clientInterface.sendError(errorMsg);
						throw new RuntimeException(errorMsg);
					}

					log.info("Polled status endpoint {} times:\n{}", i + 1, statusData);
					jobDone = statusData.get("error").asBoolean() || statusData.get("job_completed").asBoolean();
					if (jobDone) {
						clientInterface.sendMessage("COSMOS extraction complete; processing results...");
						break;
					}
					Thread.sleep(POLLING_INTERVAL_SECONDS * 1000);
				}

				if (!jobDone) {
					final String errorMsg = "Extraction job did not complete within the expected time";
					clientInterface.sendError(errorMsg);
					throw new RuntimeException(errorMsg);
				}

				final ResponseEntity<byte[]> zipFileResp = extractionProxy.result(jobId);
				if (!zipFileResp.getStatusCode().is2xxSuccessful()) {
					final String errorMsg = "Unable to fetch the extraction result";
					clientInterface.sendError(errorMsg);
					throw new RuntimeException(errorMsg);
				}

				clientInterface.sendMessage("Uploading COSMOS extraction results...");
				final String zipFileName = documentId + "_cosmos.zip";
				documentService.uploadFile(documentId, zipFileName, new ByteArrayEntity(zipFileResp.getBody()),
						ContentType.APPLICATION_OCTET_STREAM);

				document.getFileNames().add(zipFileName);

				// Open the zipfile and extract the contents
				clientInterface.sendMessage("Extracting COSMOS extraction results...");
				final Map<String, HttpEntity> fileMap = new HashMap<>();
				try {
					final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
							zipFileResp.getBody());
					final ZipInputStream zipInputStream = new ZipInputStream(byteArrayInputStream);

					ZipEntry entry = zipInputStream.getNextEntry();
					while (entry != null) {
						log.info("Adding {} to filemap", entry.getName());
						final String filenameNoExt = removeFileExtension(entry.getName());
						fileMap.put(filenameNoExt, zipEntryToHttpEntity(zipInputStream));
						entry = zipInputStream.getNextEntry();
					}

					zipInputStream.closeEntry();
					zipInputStream.close();
				} catch (final IOException e) {
					final String errorMsg = "Unable to extract the contents of the zip file";
					clientInterface.sendError(errorMsg);
					throw new RuntimeException(errorMsg, e);
				}

				final ResponseEntity<JsonNode> textResp = extractionProxy.text(jobId);
				if (!textResp.getStatusCode().is2xxSuccessful()) {
					final String errorMsg = "Unable to fetch the text extractions";
					clientInterface.sendError(errorMsg);
					throw new RuntimeException(errorMsg);
				}

				// clear existing assets
				document.setAssets(new ArrayList<>());
				clientInterface.sendMessage("Uploading COSMOS extraction assets...");

				int totalUploads = 0;

				for (final ExtractionAssetType extractionType : ExtractionAssetType.values()) {
					final ResponseEntity<JsonNode> response = extractionProxy.extraction(jobId,
							extractionType.toStringPlural());
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
							final HttpEntity file = fileMap.get(assetFilenameNoExt);
							if (file == null) {
								throw new RuntimeException("Unable to find file " + assetFileName + " in zipfile");
							}
							documentService.uploadFile(documentId, assetFileName, file, ContentType.IMAGE_JPEG);
							totalUploads++;

						} else {
							log.warn("No img_pth found in record: {}", record);
						}

						final DocumentExtraction extraction = new DocumentExtraction();
						extraction.setFileName(assetFileName);
						extraction.setAssetType(extractionType);
						extraction.setMetadata(objectMapper.convertValue(record, Map.class));

						document.getAssets().add(extraction);
						clientInterface
								.sendMessage(
										String.format("Add COSMOS extraction %s to Document...", assetFileName));
					}
				}

				log.info("Uploaded a total of {} files", totalUploads);

				String responseText = "";
				for (final JsonNode record : textResp.getBody()) {
					if (record.has("content")) {
						responseText += record.get("content").asText() + "\n";
						document.setText(responseText);
					} else {
						log.warn("No content found in record: {}", record);
					}
				}

				// update the document
				document = documentService.updateAsset(document);

				// if there is text, run variable extraction
				if (!responseText.isEmpty()) {

					// run variable extraction
					try {
						clientInterface.sendMessage("Dispatching variable extraction request...");
						document = extractVariables(documentId, new ArrayList<>(), domain).get();
						clientInterface.sendMessage("Variable extraction completed");
					} catch (final Exception e) {
						clientInterface.sendMessage("Variable extraction failed, continuing");
					}

					// check for input length
					if (document.getText().length() > ModelCardResponseHandler.MAX_TEXT_SIZE) {
						log.warn("Document {} text too long for GoLLM model card task, not sendingh request",
								documentId);
					} else {
						// dispatch GoLLM model card request
						final ModelCardResponseHandler.Input input = new ModelCardResponseHandler.Input();
						input.setResearchPaper(document.getText());

						// Create the task
						final TaskRequest req = new TaskRequest();
						req.setType(TaskRequest.TaskType.GOLLM);
						req.setScript(ModelCardResponseHandler.NAME);
						req.setInput(objectMapper.writeValueAsBytes(input));

						final ModelCardResponseHandler.Properties props = new ModelCardResponseHandler.Properties();
						props.setDocumentId(documentId);
						req.setAdditionalProperties(props);

						clientInterface.sendMessage("Sending GoLLM model card request");
						final TaskResponse resp = taskService.runTaskSync(req);
						if (resp.getStatus() != TaskStatus.SUCCESS) {
							final String errorMsg = "GoLLM model card task failed";
							clientInterface.sendError(errorMsg);
							throw new RuntimeException(errorMsg);
						}
						clientInterface.sendMessage("Model Card created");
					}
				}

				clientInterface.sendFinalMessage("Extraction complete");

				// return the final document
				return documentService.updateAsset(document);

			} catch (final FeignException e) {
				final String error = "Transitive service failure";
				log.error(error, e.contentUTF8(), e);
				throw new ResponseStatusException(
						e.status() < 100 ? HttpStatus.INTERNAL_SERVER_ERROR : HttpStatus.valueOf(e.status()),
						error + ": " + e.getMessage());
			} catch (final Exception e) {
				final String error = "Unable to extract pdf";
				log.error(error, e);
				clientInterface.sendError("Extraction failed, unexpected error.");
				throw new ResponseStatusException(
						org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
						error);
			}
		});
	}

	public Future<DocumentAsset> extractVariables(final UUID documentId, final List<UUID> modelIds,
			final String domain) {

		// Set up the client interface
		final String userId = currentUserService.get().getId();
		final ClientEventInterface clientInterface = new ClientEventInterface(clientEventService, documentId, userId,
				HALFTIME_SECONDS);

		return executor.submit(() -> {

			clientInterface.sendMessage("Starting variable extraction.");
			try {
				// Fetch the text from the document
				final DocumentAsset document = documentService.getAsset(documentId).orElseThrow();
				clientInterface.sendMessage("Document found, fetching text.");
				if (document.getText() == null || document.getText().isEmpty()) {
					throw new RuntimeException("No text found in paper document");
				}

				// add optional models
				final List<Model> models = new ArrayList<>();
				for (final UUID modelId : modelIds) {
					models.add(modelService.getAsset(modelId).orElseThrow());
				}
				clientInterface.sendMessage("Model(s) found, added to extraction request.");

				// Create a collection to hold the variable extractions
				JsonNode collection = null;

				clientInterface.sendMessage("Sending request to be processes by SKEMA and MIT.");

				final IntegratedTextExtractionsBody body = new IntegratedTextExtractionsBody(
						document.getText(),
						models);

				log.info("Sending variable extraction request to SKEMA");
				final ResponseEntity<JsonNode> resp = skemaUnifiedProxy.integratedTextExtractions(true, true, body);

				clientInterface.sendMessage("Response received.");
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

				clientInterface.sendMessage("Organizing and saving the extractions.");
				final List<JsonNode> attributes = new ArrayList<>();
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
						clientInterface.sendMessage("Attempting to align models for model: " + modelId);
						try {
							alignAMR(documentId, modelId).get();
							clientInterface.sendMessage("Model " + modelId + " aligned successfully");
						} catch (final Exception e) {
							clientInterface.sendMessage("Failed to align model: " + modelId + ", continuing...");
						}
					}
				}

				clientInterface.sendFinalMessage("Extraction complete");

				// update the document
				return documentService.updateAsset(document);

			} catch (final FeignException e) {
				final String error = "Transitive service failure";
				log.error(error, e.contentUTF8(), e);
				throw new ResponseStatusException(
						e.status() < 100 ? HttpStatus.INTERNAL_SERVER_ERROR : HttpStatus.valueOf(e.status()),
						error + ": " + e.getMessage());
			} catch (final Exception e) {
				final String error = "SKEMA unified integrated-text-extractions request from document: "
						+ documentId + " failed.";
				log.error(error, e);
				clientInterface.sendError(error + " — " + e.getMessage());
				throw new ResponseStatusException(
						org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
						error);
			}
		});
	}

	public Future<Model> alignAMR(final UUID documentId, final UUID modelId) {

		final String userId = currentUserService.get().getId();
		final ClientEventInterface clientInterface = new ClientEventInterface(clientEventService, documentId, userId,
				HALFTIME_SECONDS);

		return executor.submit(() -> {
			try {
				clientInterface.sendMessage("Starting model alignment...");

				final DocumentAsset document = documentService.getAsset(documentId).orElseThrow();

				final Model model = modelService.getAsset(modelId).orElseThrow();

				final String modelString = objectMapper.writeValueAsString(model);

				if (document.getMetadata().get("attributes") == null) {
					throw new RuntimeException("No attributes found in document");
				}

				final JsonNode attributes = objectMapper.valueToTree(document.getMetadata().get("attributes"));

				final ObjectNode extractions = objectMapper.createObjectNode();
				extractions.set("attributes", attributes);

				final String extractionsString = objectMapper.writeValueAsString(extractions);

				final StringMultipartFile amrFile = new StringMultipartFile(
						modelString,
						"amr.json",
						"application/json");
				final StringMultipartFile extractionFile = new StringMultipartFile(
						extractionsString,
						"extractions.json",
						"application/json");

				final ResponseEntity<JsonNode> res;
				try {
					res = skemaUnifiedProxy.linkAMRFile(amrFile, extractionFile);
				} catch (final FeignException e) {
					final String error = "Transitive service failure";
					log.error(error, e.contentUTF8(), e);
					throw new ResponseStatusException(
							e.status() < 100 ? HttpStatus.INTERNAL_SERVER_ERROR : HttpStatus.valueOf(e.status()),
							error + ": " + e.getMessage());
				}
				if (!res.getStatusCode().is2xxSuccessful()) {
					throw new ResponseStatusException(
							res.getStatusCode(),
							"Unable to link AMR file");
				}

				final JsonNode modelJson = objectMapper.valueToTree(model);

				// overwrite all updated fields
				JsonUtil.recursiveSetAll((ObjectNode) modelJson, res.getBody());

				// update the model
				modelService.updateAsset(model);

				// create provenance
				final Provenance provenance = new Provenance(ProvenanceRelationType.EXTRACTED_FROM, modelId,
						ProvenanceType.MODEL,
						documentId, ProvenanceType.DOCUMENT);
				provenanceService.createProvenance(provenance);

				return model;

			} catch (final FeignException e) {
				final String error = "Transitive service failure";
				log.error(error, e.contentUTF8(), e);
				throw new ResponseStatusException(
						e.status() < 100 ? HttpStatus.INTERNAL_SERVER_ERROR : HttpStatus.valueOf(e.status()),
						error + ": " + e.getMessage());
			} catch (final Exception e) {
				final String error = "SKEMA link_amr request from document: "
						+ documentId + " failed.";
				log.error(error, e);
				clientInterface.sendError(error + " — " + e.getMessage());
				throw new ResponseStatusException(
						org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
						error);
			}
		});
	}

	public static HttpEntity zipEntryToHttpEntity(final ZipInputStream zipInputStream) throws IOException {
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

		return new ByteArrayEntity(bytes);
	}
}
