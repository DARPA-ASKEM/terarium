package software.uncharted.terarium.hmiserver.service.climatedata;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import software.uncharted.terarium.hmiserver.configuration.Config;
import software.uncharted.terarium.hmiserver.models.climateData.ClimateDataPreview;
import software.uncharted.terarium.hmiserver.models.climateData.ClimateDataPreviewTask;
import software.uncharted.terarium.hmiserver.models.climateData.ClimateDataResponse;
import software.uncharted.terarium.hmiserver.models.climateData.ClimateDataResultPng;
import software.uncharted.terarium.hmiserver.proxies.climatedata.ClimateDataProxy;
import software.uncharted.terarium.hmiserver.repository.climateData.ClimateDataPreviewRepository;
import software.uncharted.terarium.hmiserver.repository.climateData.ClimateDataPreviewTaskRepository;
import software.uncharted.terarium.hmiserver.service.s3.S3ClientService;

import java.util.Base64;
import java.util.List;

@RequiredArgsConstructor
@Service
@Slf4j
public class ClimateDataService {

    final ObjectMapper objectMapper;
    final ClimateDataProxy climateDataProxy;
    final ClimateDataPreviewTaskRepository climateDataPreviewTaskRepository;
    final ClimateDataPreviewRepository climateDataPreviewRepository;
    final S3ClientService s3ClientService;
    final Config config;

    @Scheduled(fixedRate = 1000 * 60 * 2L) // every 2 minutes
    public void checkJobStatusTask() {
        final List<ClimateDataPreviewTask> previewTasks = climateDataPreviewTaskRepository.findAll();

        for (final ClimateDataPreviewTask previewTask : previewTasks) {
            final ResponseEntity<JsonNode> response = climateDataProxy.status(previewTask.getStatusId());
            final ClimateDataResponse climateDataResponse = objectMapper.convertValue(response.getBody(), ClimateDataResponse.class);
            if (climateDataResponse.getResult().getJobResult() != null) {
                final ClimateDataResultPng png = objectMapper.convertValue(climateDataResponse.getResult().getJobResult(), ClimateDataResultPng.class);
                final int index = png.getPng().indexOf(',');
                if (index > -1 && index + 1 < png.getPng().length()) {
                    final String pngBase64 = png.getPng().substring(index+1);
                    final byte[] pngBytes = Base64.getDecoder().decode(pngBase64);

                    final String bucket = config.getFileStorageS3BucketName();
                    final String key = String.join("/dataset", previewTask.getEsgfId());

                    s3ClientService.getS3Service().putObject(bucket, key, pngBytes);

                    final ClimateDataPreview preview = new ClimateDataPreview();
                    preview.setEsgfId(previewTask.getEsgfId());
                    preview.setVariableId(previewTask.getVariableId());
                    preview.setTimestamps(previewTask.getTimestamps());
                    preview.setTimeIndex(previewTask.getTimeIndex());

                    climateDataPreviewRepository.save(preview);
                }

                climateDataPreviewTaskRepository.delete(previewTask);
            }
            if (climateDataResponse.getResult().getJobError() != null) {
                final ClimateDataPreview preview = new ClimateDataPreview();
                preview.setEsgfId(previewTask.getEsgfId());
                preview.setVariableId(previewTask.getVariableId());
                preview.setTimestamps(previewTask.getTimestamps());
                preview.setTimeIndex(previewTask.getTimeIndex());
                preview.setError(climateDataResponse.getResult().getJobError().toString());
                climateDataPreviewRepository.save(preview);

                climateDataPreviewTaskRepository.delete(previewTask);
            }
        }

        // TODO: work on subset jobs
    }

    public void addPreviewJob(final String esgfId, final String variableId, final String timestamps, final String timeIndex, final String statusId) {
        final ClimateDataPreviewTask task = new ClimateDataPreviewTask();
        task.setStatusId(statusId);
        task.setEsgfId(esgfId);
        task.setVariableId(variableId);
        task.setTimestamps(timestamps);
        task.setTimeIndex(timeIndex);
        climateDataPreviewTaskRepository.save(task);
    }

    public ResponseEntity<String> getPreview(final String esgfId, final String variableId, final String timestamps, final String timeIndex) {
        final ClimateDataPreview preview = climateDataPreviewRepository.findByEsgfIdAndVariableIdAndTimestampsAndTimeIndex(esgfId, variableId, timestamps, timeIndex);
        if (preview != null) {
            if (preview.getError() != null) {
                return ResponseEntity.internalServerError().body(preview.getError());
            }
            // TODO: what is this url
            final String pngUrl = "";
            return ResponseEntity.ok(pngUrl);
        }
        final ClimateDataPreviewTask task = climateDataPreviewTaskRepository.findByEsgfIdAndVariableIdAndTimestampsAndTimeIndex(esgfId, variableId, timestamps, timeIndex);
        if (task != null) {
            return ResponseEntity.accepted().build();
        }
        return null;
    }

    public void addSubsetJob(final String esgfId, final String envelope, final String timestamps, final String thinFactor, final String statusId) {
    }

    public static JsonNode getSubsetJob(final String esgfId, final String envelope, final String timestamps, final String thinFactor) {
        return null;
    }
}
