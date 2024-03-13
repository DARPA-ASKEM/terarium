package software.uncharted.terarium.hmiserver.proxies.climatedata;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import software.uncharted.terarium.hmiserver.models.climateData.ClimateDataTask;
import software.uncharted.terarium.hmiserver.repository.climateData.ClimateDataTaskRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ClimateDataStatusScheduledTask {

    private final ClimateDataProxy climateDataProxy;
    private final ClimateDataTaskRepository climateDataTaskRepository;

    @Scheduled(fixedRate = 1000 * 60 * 2L) // every 2 minutes
    public void checkJobStatusTask() {
        List<ClimateDataTask> climateDataTasks = climateDataTaskRepository.findAll();
        log.info("TODO: Get jobs from database");

        for (ClimateDataTask climateDataTask : climateDataTasks) {
            final ResponseEntity<JsonNode> response = climateDataProxy.status(climateDataTask.getClimateDataId());
            log.info("TODO: if job complete write response to database as a data asset, then delete task");
        }
    }

    public void checkJob(String id) {
        log.info("TODO: write id to database");
        ClimateDataTask climateDataTask = new ClimateDataTask();
        climateDataTask.setClimateDataId(id);
        climateDataTaskRepository.save(climateDataTask);
    }
}
