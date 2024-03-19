package software.uncharted.terarium.hmiserver.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import software.uncharted.terarium.hmiserver.controller.services.DownloadService;
import software.uncharted.terarium.hmiserver.models.ClientEvent;
import software.uncharted.terarium.hmiserver.models.ClientEventType;
import software.uncharted.terarium.hmiserver.models.dataservice.AssetType;
import software.uncharted.terarium.hmiserver.models.dataservice.PresignedURL;
import software.uncharted.terarium.hmiserver.models.dataservice.document.DocumentAsset;
import software.uncharted.terarium.hmiserver.models.dataservice.document.DocumentExtraction;
import software.uncharted.terarium.hmiserver.models.dataservice.document.ExtractionAssetType;
import software.uncharted.terarium.hmiserver.models.dataservice.project.Project;
import software.uncharted.terarium.hmiserver.models.documentservice.Document;
import software.uncharted.terarium.hmiserver.models.documentservice.Extraction;
import software.uncharted.terarium.hmiserver.models.documentservice.responses.XDDExtractionsResponseOK;
import software.uncharted.terarium.hmiserver.models.documentservice.responses.XDDResponse;
import software.uncharted.terarium.hmiserver.models.extractionservice.ExtractionStatusUpdate;
import software.uncharted.terarium.hmiserver.models.task.TaskRequest;
import software.uncharted.terarium.hmiserver.proxies.documentservice.ExtractionProxy;
import software.uncharted.terarium.hmiserver.service.data.DocumentAssetService;
import software.uncharted.terarium.hmiserver.service.data.ProjectAssetService;
import software.uncharted.terarium.hmiserver.service.tasks.ModelCardResponseHandler;
import software.uncharted.terarium.hmiserver.utils.ByteMultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExtractionService {
    private final DocumentAssetService documentService;
    private final ExtractionProxy extractionProxy;
    private final ObjectMapper objectMapper;
    private final ClientEventService clientEventService;
    private final DocumentAssetService documentAssetService;
    private final ProjectAssetService projectAssetService;

    private ExecutorService executor = Executors.newFixedThreadPool(1);

    private static final Integer TOTAL_EXTRACTION_STEPS = 14;

    @Value("${xdd.api-key}")
    String apikey;

    public void createDocumentFromXDD(Document document, Optional<Project> project) {
        executor.execute(() -> {
            try {
                final String doi = DocumentAsset.getDocumentDoi(document);
                final String projectUserId = project.get().getUserId();
                // get pdf url and filename
                final String fileUrl = DownloadService.getPDFURL("https://unpaywall.org/" + doi);
                final String filename = DownloadService.pdfNameFromUrl(fileUrl);

                final XDDResponse<XDDExtractionsResponseOK> extractionResponse = extractionProxy.getExtractions(doi, null,
                        null,
                        null,
                        null, apikey);

                // create a new document asset from the metadata in the xdd document and write
                // it to the db
                DocumentAsset documentAsset = createDocumentAssetFromXDDDocument(document, projectUserId,
                        extractionResponse.getSuccess().getData());
                if (filename != null) {
                    documentAsset.getFileNames().add(filename);
                    documentAsset = documentAssetService.updateAsset(documentAsset).get();
                }

                // add asset to project
                projectAssetService.createProjectAsset(project.get(), AssetType.DOCUMENT, documentAsset);

                // Upload the PDF from unpaywall
                uploadPDFFileToDocumentThenExtract(doi, filename, documentAsset.getId(), projectUserId);
            } catch (final IOException | URISyntaxException e) {
                final String error = "Unable to upload document from xdd";
                log.error(error, e);
                throw new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        error);
            }
        });
    }

    public void extractPDF(UUID documentId, String userId) {
        executor.execute(() -> {
            try {
                updateClient(documentId, 0, TOTAL_EXTRACTION_STEPS, userId);
                DocumentAsset document = documentService.getAsset(documentId).get();
                updateClient(documentId, 1, TOTAL_EXTRACTION_STEPS, userId);

                if (document.getFileNames().isEmpty()) {
                    String errorMsg = "No files found on document";
                    updateClientError(documentId, 1, TOTAL_EXTRACTION_STEPS, errorMsg, userId);
                    throw new RuntimeException(errorMsg);
                }

                final String filename = document.getFileNames().get(0);

                final byte[] documentContents = documentService.fetchFileAsBytes(documentId, filename).get();
                updateClient(documentId, 2, TOTAL_EXTRACTION_STEPS, userId);

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
                updateClient(documentId, 3, TOTAL_EXTRACTION_STEPS, userId);
                for (int i = 0; i < MAX_ITERATIONS; i++) {

                    final ResponseEntity<JsonNode> statusResp = extractionProxy.status(jobId);
                    if (!statusResp.getStatusCode().is2xxSuccessful()) {
                        String errorMsg = "Unable to poll status endpoint";
                        updateClientError(documentId, 3, TOTAL_EXTRACTION_STEPS, errorMsg, userId);
                        throw new RuntimeException(errorMsg);
                    }

                    final JsonNode statusData = statusResp.getBody();
                    if (!statusData.get("error").isNull()) {
                        String errorMsg = "Extraction job failed: " + statusData.has("error");
                        updateClientError(documentId, 3, TOTAL_EXTRACTION_STEPS, errorMsg, userId);
                        throw new RuntimeException(errorMsg);
                    }

                    log.info("Polled status endpoint {} times:\n{}", i + 1, statusData);
                    jobDone = statusData.get("error").asBoolean() || statusData.get("job_completed").asBoolean();
                    if (jobDone) {
                        updateClient(documentId, 4, TOTAL_EXTRACTION_STEPS, "PDF extraction complete; processing results...", userId);
                        break;
                    }
                    Thread.sleep(POLLING_INTERVAL_SECONDS * 1000);
                }

                if (!jobDone) {
                    String errorMsg = "Extraction job did not complete within the expected time";
                    updateClientError(documentId, 5, TOTAL_EXTRACTION_STEPS, errorMsg, userId);
                    throw new RuntimeException(errorMsg);
                }

                final ResponseEntity<byte[]> zipFileResp = extractionProxy.result(jobId);
                if (!zipFileResp.getStatusCode().is2xxSuccessful()) {
                    String errorMsg = "Unable to fetch the extraction result";
                    updateClientError(documentId, 6, TOTAL_EXTRACTION_STEPS, errorMsg, userId);
                    throw new RuntimeException(errorMsg);
                }

                updateClient(documentId, 7, TOTAL_EXTRACTION_STEPS, userId);
                final String zipFileName = documentId + "_cosmos.zip";
                documentService.uploadFile(documentId, zipFileName, new ByteArrayEntity(zipFileResp.getBody()));

                document.getFileNames().add(zipFileName);

                // Open the zipfile and extract the contents

                updateClient(documentId, 8, TOTAL_EXTRACTION_STEPS, userId);
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
                    String errorMsg = "Unable to extract the contents of the zip file";
                    updateClientError(documentId, 8, TOTAL_EXTRACTION_STEPS, errorMsg, userId);
                    throw new RuntimeException(errorMsg, e);
                }

                final ResponseEntity<JsonNode> textResp = extractionProxy.text(jobId);
                if (!textResp.getStatusCode().is2xxSuccessful()) {
                    String errorMsg = "Unable to fetch the text extractions";
                    updateClientError(documentId, 9, TOTAL_EXTRACTION_STEPS, errorMsg, userId);
                    throw new RuntimeException(errorMsg);
                }

                // clear existing assets
                document.setAssets(new ArrayList<>());
                updateClient(documentId, 10, TOTAL_EXTRACTION_STEPS, userId);

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
                        extraction.setMetadata(objectMapper.convertValue(record, Map.class));

                        document.getAssets().add(extraction);
                        updateClient(documentId, 11, TOTAL_EXTRACTION_STEPS, userId);
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
                document = documentService.updateAsset(document).get();
                updateClient(documentId, 12, TOTAL_EXTRACTION_STEPS, "Document assets completed", userId);

                if (document.getText() == null || document.getText().isEmpty()) {
                    log.warn("Document {} has no text to send", documentId);
                    updateClientError(documentId, 13, TOTAL_EXTRACTION_STEPS, "Model Card not created: document has no text", userId);
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Document has no text");
                }

                // check for input length
                if (document.getText().length() > 600000) {
                    log.warn("Document {} text too long for GoLLM model card task", documentId);
                    updateClientError(documentId, 13, TOTAL_EXTRACTION_STEPS, "Model Card not created: document text is too long", userId);
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Document text is too long");
                }

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
                updateClient(documentId, 14, TOTAL_EXTRACTION_STEPS, "Extraction complete.", userId);
            } catch (final Exception e) {
                final String error = "Unable to extract pdf";
                log.error(error, e);
                updateClientError(documentId, 14, TOTAL_EXTRACTION_STEPS, "Extraction failed, unexpected error.", userId);
                throw new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        error);
            }
        });
    }

    private void updateClient(UUID documentId, Integer step, Integer totalSteps, String userId) {
        updateClient(documentId, step, totalSteps, null, null, userId);
    }

    private void updateClient(UUID documentId, Integer step, Integer totalSteps, String message, String userId) {
        updateClient(documentId, step, totalSteps, message, null, userId);
    }

    private void updateClientError(UUID documentId, Integer step, Integer totalSteps, String error, String userId) {
        updateClient(documentId, step, totalSteps, null, error, userId);
    }

    private void updateClient(UUID documentId, Integer step, Integer totalSteps, String message, String error, String userId) {
        ExtractionStatusUpdate update = new ExtractionStatusUpdate(documentId, step, totalSteps, message, error);
        ClientEvent<ExtractionStatusUpdate> status =
                ClientEvent.<ExtractionStatusUpdate>builder().type(ClientEventType.EXTRACTION).data(update).build();
        clientEventService.sendToUser(status, userId);
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

    /**
     * Creates a document asset from an XDD document
     *
     * @param document    xdd document
     * @param userId      current user name
     * @param extractions list of extractions associated with the document
     * @return document asset
     */
    private DocumentAsset createDocumentAssetFromXDDDocument(
            final Document document,
            final String userId,
            final List<Extraction> extractions) throws IOException {
        final String name = document.getTitle();

        // create document asset
        final DocumentAsset documentAsset = new DocumentAsset();
        documentAsset.setName(name);
        documentAsset.setDescription(name);
        documentAsset.setUserId(userId);
        documentAsset.setFileNames(new ArrayList<>());

        if (extractions != null) {
            documentAsset.setAssets(new ArrayList<>());
            for (int i = 0; i < extractions.size(); i++) {
                final Extraction extraction = extractions.get(i);
                if (extraction.getAskemClass().equalsIgnoreCase(ExtractionAssetType.FIGURE.toString())
                        || extraction.getAskemClass().equalsIgnoreCase(ExtractionAssetType.TABLE.toString())
                        || extraction.getAskemClass().equalsIgnoreCase(ExtractionAssetType.EQUATION.toString())) {
                    final DocumentExtraction documentExtraction = new DocumentExtraction().setMetadata(new HashMap<>());
                    documentExtraction.setAssetType(ExtractionAssetType.fromString(extraction.getAskemClass()));
                    documentExtraction.setFileName("extraction_" + i + ".png");
                    documentExtraction.getMetadata().put("title", extraction.getProperties().getTitle());
                    documentExtraction.getMetadata().put("description", extraction.getProperties().getCaption());
                    documentAsset.getAssets().add(documentExtraction);
                    documentAsset.getFileNames().add(documentExtraction.getFileName());
                }
            }

        }

        if (document.getGithubUrls() != null && !document.getGithubUrls().isEmpty()) {
            documentAsset.setMetadata(new HashMap<>());
            documentAsset.getMetadata().put("github_urls", document.getGithubUrls());
        }

        return documentAssetService.createAsset(documentAsset);
    }

    /**
     * Uploads a PDF file to a document asset and then fires and forgets the
     * extraction
     *
     * @param doi      DOI of the document
     * @param filename filename of the PDF
     * @param docId    document id
     * @return extraction job id
     */
    private void uploadPDFFileToDocumentThenExtract(final String doi, final String filename,
                                                    final UUID docId, final String userId) {
        try (final CloseableHttpClient httpclient = HttpClients.custom()
                .disableRedirectHandling()
                .build()) {

            final byte[] fileAsBytes = DownloadService.getPDF("https://unpaywall.org/" + doi);

            // if this service fails, return ok with errors
            if (fileAsBytes == null || fileAsBytes.length == 0) {
                throw new ResponseStatusException(
                        org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
                        "Document has no data, empty bytes");
            }

            // upload pdf to document asset
            final HttpEntity fileEntity = new ByteArrayEntity(fileAsBytes, ContentType.APPLICATION_OCTET_STREAM);
            final PresignedURL presignedURL = documentAssetService.getUploadUrl(docId, filename);
            final HttpPut put = new HttpPut(presignedURL.getUrl());
            put.setEntity(fileEntity);
            final HttpResponse pdfUploadResponse = httpclient.execute(put);

            if (pdfUploadResponse.getStatusLine().getStatusCode() >= HttpStatus.BAD_REQUEST.value()) {
                throw new ResponseStatusException(
                        org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
                        "Unable to upload document");
            }

            // fire and forgot pdf extractions
            extractPDF(docId, userId);
        } catch (final Exception e) {
            log.error("Unable to upload PDF document then extract", e);
        }
    }
}
