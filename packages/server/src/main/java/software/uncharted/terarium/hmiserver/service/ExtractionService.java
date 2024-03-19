package software.uncharted.terarium.hmiserver.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ByteArrayEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import software.uncharted.terarium.hmiserver.models.ClientEvent;
import software.uncharted.terarium.hmiserver.models.ClientEventType;
import software.uncharted.terarium.hmiserver.models.dataservice.document.DocumentAsset;
import software.uncharted.terarium.hmiserver.models.dataservice.document.DocumentExtraction;
import software.uncharted.terarium.hmiserver.models.dataservice.document.ExtractionAssetType;
import software.uncharted.terarium.hmiserver.models.extractionservice.ExtrationStatusUpdate;
import software.uncharted.terarium.hmiserver.models.simulationservice.CiemssStatusUpdate;
import software.uncharted.terarium.hmiserver.models.task.TaskRequest;
import software.uncharted.terarium.hmiserver.proxies.documentservice.ExtractionProxy;
import software.uncharted.terarium.hmiserver.service.data.DocumentAssetService;
import software.uncharted.terarium.hmiserver.service.tasks.ModelCardResponseHandler;
import software.uncharted.terarium.hmiserver.utils.ByteMultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExtractionService {
    final DocumentAssetService documentService;
    final ExtractionProxy extractionProxy;
    final ObjectMapper objectMapper;
    final ClientEventService clientEventService;

    private static final Integer TOTAL_EXTRACTION_STEPS = 14;

    public void extractPDF(UUID documentId, String userId) {

        try {
            updateClient(documentId, 0, TOTAL_EXTRACTION_STEPS, userId);
            DocumentAsset document = documentService.getAsset(documentId).orElseThrow();
            updateClient(documentId, 1, TOTAL_EXTRACTION_STEPS, userId);

            if (document.getFileNames().isEmpty()) {
                String errorMsg = "No files found on document";
                updateClientError(documentId, 1, TOTAL_EXTRACTION_STEPS, errorMsg, userId);
                throw new RuntimeException(errorMsg);
            }

            final String filename = document.getFileNames().get(0);

            final byte[] documentContents = documentService.fetchFileAsBytes(documentId, filename).orElseThrow();
            updateClient(documentId, 2, TOTAL_EXTRACTION_STEPS, userId);

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
            document = documentService.updateAsset(document).orElseThrow();
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
                    org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
                    error);
        }
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
        ExtrationStatusUpdate update = new ExtrationStatusUpdate(documentId, step, totalSteps, message, error);
        ClientEvent<ExtrationStatusUpdate> status =
                ClientEvent.<ExtrationStatusUpdate>builder().type(ClientEventType.EXTRACTION).data(update).build();
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
}
