package software.uncharted.terarium.hmiserver.service.climatedata;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import software.uncharted.terarium.hmiserver.models.climateData.ClimateDataPreviewTask;
import software.uncharted.terarium.hmiserver.proxies.climatedata.ClimateDataProxy;
import software.uncharted.terarium.hmiserver.repository.climateData.ClimateDataPreviewTaskRepository;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ClimateDataService {

    private final ClimateDataProxy climateDataProxy;
    private final ClimateDataPreviewTaskRepository climateDataPreviewTaskRepository;

    @Scheduled(fixedRate = 1000 * 60 * 2L) // every 2 minutes
    public void checkJobStatusTask() {
        List<ClimateDataPreviewTask> previewTasks = climateDataPreviewTaskRepository.findAll();
        log.info("TODO: Get jobs from database");

        for (ClimateDataPreviewTask previewTask : previewTasks) {
            final ResponseEntity<JsonNode> response = climateDataProxy.status(previewTask.getClimateDataId());
            log.info("TODO: if job complete write response to database as a data asset, then delete task");
        }
    }

    public void addPreviewJob(String datasetId, String variableId, String timestamps, String timeIndex, String id) {
        ClimateDataPreviewTask task = new ClimateDataPreviewTask();
        task.setClimateDataId(id);
        task.setDatasetId(datasetId);
        task.setVariableId(variableId);
        task.setTimestamps(timestamps);
        task.setTimeIndex(timeIndex);
        climateDataPreviewTaskRepository.save(task);
    }

    public String getPreviewJob(String datasetId, String variableId, String timestamps, String timeIndex) {
        ClimateDataPreviewTask task = climateDataPreviewTaskRepository.findByDatasetIdAndVariableIdAndTimestampsAndTimeIndex(datasetId, variableId, timestamps, timeIndex);
        return null;
    }

    public void addSubsetJob(String datasetId, String envelope, String timestamps, String thinFactor, String id) {
    }

    public JsonNode getSubsetJob(String datasetId, String envelope, String timestamps, String thinFactor) {
        return null;
    }
}
