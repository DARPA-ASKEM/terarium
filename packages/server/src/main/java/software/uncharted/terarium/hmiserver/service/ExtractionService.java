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

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.uncharted.terarium.hmiserver.models.ClientEvent;
import software.uncharted.terarium.hmiserver.models.ClientEventType;
import software.uncharted.terarium.hmiserver.models.dataservice.document.DocumentAsset;
import software.uncharted.terarium.hmiserver.models.dataservice.document.DocumentExtraction;
import software.uncharted.terarium.hmiserver.models.dataservice.document.ExtractionAssetType;
import software.uncharted.terarium.hmiserver.models.extractionservice.ExtractionStatusUpdate;
import software.uncharted.terarium.hmiserver.models.task.TaskRequest;
import software.uncharted.terarium.hmiserver.models.task.TaskResponse;
import software.uncharted.terarium.hmiserver.models.task.TaskStatus;
import software.uncharted.terarium.hmiserver.proxies.documentservice.ExtractionProxy;
import software.uncharted.terarium.hmiserver.proxies.mit.MitProxy;
import software.uncharted.terarium.hmiserver.proxies.skema.SkemaUnifiedProxy;
import software.uncharted.terarium.hmiserver.proxies.skema.SkemaUnifiedProxy.IntegratedTextExtractionsBody;
import software.uncharted.terarium.hmiserver.service.data.DocumentAssetService;
import software.uncharted.terarium.hmiserver.service.tasks.ModelCardResponseHandler;
import software.uncharted.terarium.hmiserver.service.tasks.TaskService;
import software.uncharted.terarium.hmiserver.utils.ByteMultipartFile;
import software.uncharted.terarium.hmiserver.utils.StringMultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExtractionService {
	final DocumentAssetService documentService;
	final ExtractionProxy extractionProxy;
	final SkemaUnifiedProxy skemaUnifiedProxy;
	final MitProxy mitProxy;
	final ObjectMapper objectMapper;
	final ClientEventService clientEventService;
	final TaskService taskService;

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

	public String removeFileExtension(final String filename) {
		final int lastIndexOfDot = filename.lastIndexOf(".");
		if (lastIndexOfDot != -1) {
			return filename.substring(0, lastIndexOfDot);
		}
		return filename;
	}

	public void extractPDF(final UUID documentId, final String userId, final String domain) {

		// time the progress takes to reach each subsequent half
		final Double HALFTIME_SECONDS = 2.0;

		final ClientEventInterface clientInterface = new ClientEventInterface(clientEventService, documentId, userId,
				HALFTIME_SECONDS);

		executor.execute(new Runnable() {
			@Override
			public void run() {
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
							useCache,
							documentFile);

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
					document = documentService.updateAsset(document).get();

					// if there is text, run variable extraction
					if (!responseText.isEmpty()) {

						// run variable extraction (disabled currently due to timeouts...)
						// clientInterface.sendMessage("Dispatching variable extraction request...");
						// document = extractVariables(documentId, true, true, domain);
						// clientInterface.sendMessage("Variable extraction completed");

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
				} catch (final FeignException e) {
					final String error = "Transitive service failure";
					throw new ResponseStatusException(
							HttpStatus.valueOf(e.status()),
							error + ": " + e.getMessage());
				} catch (final Exception e) {
					final String error = "Unable to extract pdf";
					log.error(error, e);
					clientInterface.sendError("Extraction failed, unexpected error.");
					throw new ResponseStatusException(
							org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
							error);
				}
			}
		});
	}

	public DocumentAsset extractVariables(final UUID documentId, final Boolean annotateSkema, final Boolean annotateMIT,
			final String domain) throws IOException {

		DocumentAsset document = documentService.getAsset(documentId).orElseThrow();

		if (document.getText() == null || document.getText().isEmpty()) {
			throw new RuntimeException("No text found in paper document");
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

		} catch (final FeignException e) {
			final String error = "SKEMA integrated-text-extractions request failed";
			throw new ResponseStatusException(
					HttpStatus.valueOf(e.status()),
					error + ": " + e.getMessage());
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

		} catch (final FeignException e) {
			final String error = "MIT upload_file_extract request failed";
			throw new ResponseStatusException(
					HttpStatus.valueOf(e.status()),
					error + ": " + e.getMessage());
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
					objectMapper.writeValueAsString(skemaCollection),
					"text.json",
					"application/json");

			final StringMultipartFile mitFile = new StringMultipartFile(
					objectMapper.writeValueAsString(mitCollection),
					"text.json",
					"application/json");

			try {
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
			} catch (final FeignException e) {
				final String error = "MIT get_mapping request failed";
				throw new ResponseStatusException(
						HttpStatus.valueOf(e.status()),
						error + ": " + e.getMessage());
			} catch (final Exception e) {
				log.error("MIT merge failed", e);
			}
		}

		// add the attributes to the metadata
		if (document.getMetadata() == null) {
			document.setMetadata(new HashMap<>());
		}
		document.getMetadata().put("attributes", attributes);

		// update the document
		document = documentService.updateAsset(document).orElseThrow();

		return document;
	}

	public HttpEntity zipEntryToHttpEntity(final ZipInputStream zipInputStream) throws IOException {
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
