package software.uncharted.terarium.hmiserver.service.climatedata;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jodd.cli.Cli;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import software.uncharted.terarium.hmiserver.configuration.Config;
import software.uncharted.terarium.hmiserver.models.climateData.*;
import software.uncharted.terarium.hmiserver.proxies.climatedata.ClimateDataProxy;
import software.uncharted.terarium.hmiserver.repository.climateData.ClimateDataPreviewRepository;
import software.uncharted.terarium.hmiserver.repository.climateData.ClimateDataPreviewTaskRepository;
import software.uncharted.terarium.hmiserver.repository.climateData.ClimateDataSubsetRepository;
import software.uncharted.terarium.hmiserver.repository.climateData.ClimateDataSubsetTaskRepository;
import software.uncharted.terarium.hmiserver.service.s3.S3ClientService;

import java.util.Base64;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
@Slf4j
public class ClimateDataService {

    final ObjectMapper objectMapper;
    final ClimateDataProxy climateDataProxy;
    final ClimateDataPreviewTaskRepository climateDataPreviewTaskRepository;
    final ClimateDataPreviewRepository climateDataPreviewRepository;
    final ClimateDataSubsetRepository climateDataSubsetRepository;
    final ClimateDataSubsetTaskRepository climateDataSubsetTaskRepository;
    final S3ClientService s3ClientService;
    final Config config;

    private final static long EXPIRATION = 60;

    @Scheduled(fixedRate = 1000 * 15 ) // every 15 seconds
    public void checkPreviewTaskStatus() {
        final List<ClimateDataPreviewTask> previewTasks = climateDataPreviewTaskRepository.findAll();

        for (final ClimateDataPreviewTask previewTask : previewTasks) {
            final ResponseEntity<JsonNode> response = climateDataProxy.status(previewTask.getStatusId());
            final ClimateDataResponse climateDataResponse = objectMapper.convertValue(response.getBody(), ClimateDataResponse.class);
            if (climateDataResponse.getResult().getJobResult() != null) {
                final ClimateDataResultPng png = objectMapper.convertValue(climateDataResponse.getResult().getJobResult(), ClimateDataResultPng.class);
                if (png != null && png.getPng() != null) {
                    final int index = png.getPng().indexOf(',');
                    if (index > -1 && index + 1 < png.getPng().length()) {
                        final String pngBase64 = png.getPng().substring(index + 1);
                        final byte[] pngBytes = Base64.getDecoder().decode(pngBase64);

                        final String bucket = config.getFileStorageS3BucketName();
                        final String key = getPreviewFilename(previewTask.getEsgfId(), previewTask.getVariableId());

                        s3ClientService.getS3Service().putObject(bucket, key, pngBytes);

                        final ClimateDataPreview preview = new ClimateDataPreview(previewTask);

                        climateDataPreviewRepository.save(preview);
                    }
                } else {
                    log.error("Failed to extract png");
                    final ClimateDataPreview preview = new ClimateDataPreview(previewTask, "Failed to extract PNG from Result: " + climateDataResponse.getResult().getJobResult());
                    climateDataPreviewRepository.save(preview);

                    climateDataPreviewTaskRepository.delete(previewTask);
                }

                climateDataPreviewTaskRepository.delete(previewTask);
            }
            if (climateDataResponse.getResult().getJobError() != null) {
                final ClimateDataPreview preview = new ClimateDataPreview(previewTask, climateDataResponse.getResult().getJobError());
                climateDataPreviewRepository.save(preview);

                climateDataPreviewTaskRepository.delete(previewTask);
            }
        }
    }

    @Scheduled(fixedRate = 1000 * 60 * 2L) // every 2 minutes
    public void checkSubsetTaskStatus() {
        final List<ClimateDataSubsetTask> subsetTasks = climateDataSubsetTaskRepository.findAll();

        for (final ClimateDataSubsetTask subsetTask : subsetTasks) {
            final ResponseEntity<JsonNode> response = climateDataProxy.status(subsetTask.getStatusId());
            log.info("Received Subset Response: " + response.getBody().toString());
            final ClimateDataResponse climateDataResponse = objectMapper.convertValue(response.getBody(), ClimateDataResponse.class);
            if (!climateDataResponse.getResult().getJobResult().isNull()) {
                log.info("Received Subset Response: " + climateDataResponse.getResult().getJobResult().toString());
                final String bucket = config.getFileStorageS3BucketName();
                final String key = getPreviewFilename(subsetTask.getEsgfId(), subsetTask.getEnvelope());

                s3ClientService.getS3Service().putObject(bucket, key, climateDataResponse.getResult().getJobResult().toString().getBytes());

                final ClimateDataSubset subset = new ClimateDataSubset(subsetTask);

                climateDataSubsetRepository.save(subset);

                climateDataSubsetTaskRepository.delete(subsetTask);
            }
            if (!climateDataResponse.getResult().getJobError().isNull()) {
                log.info("Received Subset Response: " + climateDataResponse.getResult().getJobResult().toString());
                final ClimateDataSubset subset = new ClimateDataSubset(subsetTask, climateDataResponse.getResult().getJobError());
                climateDataSubsetRepository.save(subset);

                climateDataSubsetTaskRepository.delete(subsetTask);
            }
        }
    }

    private String getPreviewFilename(String esgfId, String variableId) {
        return String.join("/preview", esgfId, variableId);
    }

    public void addPreviewJob(final String esgfId, final String variableId, final String timestamps, final String timeIndex, final String statusId) {
        final ClimateDataPreviewTask task = new ClimateDataPreviewTask(statusId, esgfId, variableId, timestamps, timeIndex);
        climateDataPreviewTaskRepository.save(task);
    }

    public ResponseEntity<String> getPreview(final String esgfId, final String variableId, final String timestamps, final String timeIndex) {
        final List<ClimateDataPreview> previews = climateDataPreviewRepository.findByEsgfIdAndVariableIdAndTimestampsAndTimeIndex(esgfId, variableId, timestamps, timeIndex);
        if (previews != null && previews.size() > 0) {
            ClimateDataPreview preview = previews.get(0);
            // find successful preview
            for (ClimateDataPreview p : previews) {
                if (p.getError() == null) {
                    preview = p;
                }
            }
            if (preview.getError() != null) {
                return ResponseEntity.internalServerError().body(preview.getError());
            }
            final String filename = getPreviewFilename(preview.getEsgfId(), preview.getVariableId());
            final Optional<String> url = s3ClientService.getS3Service().getS3PreSignedGetUrl(config.getFileStorageS3BucketName(), filename, EXPIRATION);
            if (url.isPresent()) {
                return ResponseEntity.ok(url.get());
            }
            return ResponseEntity.internalServerError().body("Failed to generate presigned s3 url");
        }
        final ClimateDataPreviewTask task = climateDataPreviewTaskRepository.findByEsgfIdAndVariableIdAndTimestampsAndTimeIndex(esgfId, variableId, timestamps, timeIndex);
        if (task != null) {
            return ResponseEntity.accepted().build();
        }
        return null;
    }

    public void addSubsetJob(final String esgfId, final String envelope, final String timestamps, final String thinFactor, final String statusId) {
        final ClimateDataSubsetTask task = new ClimateDataSubsetTask(statusId, esgfId, envelope, timestamps, thinFactor);
        climateDataSubsetTaskRepository.save(task);
    }

    public ResponseEntity<String> getSubset(final String esgfId, final String envelope, final String timestamps, final String thinFactor) {
        final List<ClimateDataSubset> subsets = climateDataSubsetRepository.findByEsgfIdAndEnvelopeAndTimestampsAndThinFactor(esgfId, envelope, timestamps, thinFactor);
        if (subsets != null && subsets.size() > 0) {
            ClimateDataSubset subset = subsets.get(0);
            // find successful subset
            for (ClimateDataSubset s : subsets) {
                if (s.getError() == null) {
                    subset = s;
                }
            }
            if (subset.getError() != null) {
                return ResponseEntity.internalServerError().body(subset.getError());
            }
            final String filename = getPreviewFilename(subset.getEsgfId(), subset.getEnvelope());
            final Optional<String> url = s3ClientService.getS3Service().getS3PreSignedGetUrl(config.getFileStorageS3BucketName(), filename, EXPIRATION);
            if (url.isPresent()) {
                return ResponseEntity.ok(url.get());
            }
            return ResponseEntity.internalServerError().body("Failed to generate presigned s3 url");
        }
        final ClimateDataSubsetTask task = climateDataSubsetTaskRepository.findByEsgfIdAndEnvelopeAndTimestampsAndThinFactor(esgfId, envelope, timestamps, thinFactor);
        if (task != null) {
            return ResponseEntity.accepted().build();
        }
        return null;
    }
}
