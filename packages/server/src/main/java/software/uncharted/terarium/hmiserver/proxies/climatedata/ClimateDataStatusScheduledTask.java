package software.uncharted.terarium.hmiserver.proxies.climatedata;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ClimateDataStatusScheduledTask {

    private final ClimateDataProxy climateDataProxy;

    private final List<String> jobIds = new ArrayList<>();

    @Scheduled(fixedRate = 1000 * 60 * 2L) // every 2 minutes
    public void checkJobStatusTask() {
        log.info("TODO: Get jobs from database");

        for (String id : jobIds) {
            final ResponseEntity<JsonNode> response = climateDataProxy.status(id);
            log.info("TODO: if job complete write response to database as a data asset");
        }
    }

    public void checkJob(String id) {
        log.info("TODO: write id to database");
        jobIds.add(id);
    }
}
