package software.uncharted.terarium.hmiserver.service.gollm;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import software.uncharted.terarium.hmiserver.models.dataservice.dataset.Dataset;
import software.uncharted.terarium.hmiserver.models.dataservice.dataset.DatasetColumn;
import software.uncharted.terarium.hmiserver.models.task.TaskRequest;
import software.uncharted.terarium.hmiserver.models.task.TaskRequest.TaskType;
import software.uncharted.terarium.hmiserver.models.task.TaskResponse;
import software.uncharted.terarium.hmiserver.service.data.DatasetService;
import software.uncharted.terarium.hmiserver.service.tasks.TaskService;
import software.uncharted.terarium.hmiserver.utils.Messages;
import software.uncharted.terarium.hmiserver.utils.rebac.Schema;

@Service
@Slf4j
@RequiredArgsConstructor
@Async
public class DatasetStatistics {

	private final ObjectMapper objectMapper;
	private final TaskService taskService;
	private final DatasetService datasetService;
	final Messages messages;

	@Data
	public static class DatasetStatisticsRequest {

		private UUID datasetId;
	}

	// Following is based on the response from the Gollm service
	// packages/gollm/tasks/dataset_statistics.py
	@Data
	public static class DatasetStatisticsResponse {

		private Map<String, NumericColumnStats> numericColumns;
		private Map<String, NonNumericColumnStats> nonNumericColumns;
		private int totalRows;
		private int totalColumns;
	}

	@Data
	public static class NumericColumnStats {

		private String dataType;
		private Double mean;
		private Double median;
		private Double min;
		private Double max;
		private Double stdDev;
		private List<Double> quartiles;
		private int uniqueValues;
		private int missingValues;
		private List<Double> histogramBins;
	}

	@Data
	public static class NonNumericColumnStats {

		private String dataType;
		private int uniqueValues;
		private Map<String, Long> mostCommon;
		private int missingValues;
	}

	/**
	 * Add statistics to the columns of a Dataset
	 * @param datasetId - The UUID of the Dataset
	 * @throws IllegalArgumentException - If the Dataset does not exist or the statistics have already been calculated
	 * @throws IOException - If there is an error reading the response from the Gollm service
	 * @throws ExecutionException - If there is an error running the Task
	 * @throws InterruptedException - If the Task is interrupted
	 * @throws TimeoutException - If the Task times out
	 */
	public void add(final UUID datasetId)
		throws IllegalArgumentException, IOException, ExecutionException, InterruptedException, TimeoutException {
		// Check that the Dataset exists
		final Dataset dataset = datasetService
			.getAsset(datasetId, Schema.Permission.READ)
			.orElseThrow(() -> new IllegalArgumentException(messages.get("dataset.not-found")));

		// Check that the Statistics have already been calculated, as Dataset are immutable
		final boolean isStatisticsExist = dataset.getColumns().stream().anyMatch(column -> column.getMetadata() != null);
		if (isStatisticsExist) {
			throw new IllegalArgumentException(messages.get("dataset.statistics.exist"));
		}

		// Create the request to the Gollm service
		final DatasetStatisticsRequest request = new DatasetStatisticsRequest();
		request.setDatasetId(datasetId);

		final TaskRequest taskRequest = new TaskRequest();
		taskRequest.setType(TaskType.GOLLM);
		taskRequest.setInput(request);
		taskRequest.setScript("gollm:dataset_statistics");

		// Get the response from the Gollm service
		final TaskResponse taskResponse = taskService.runTaskSync(taskRequest);
		final byte[] outputBytes = taskResponse.getOutput();
		final JsonNode output = objectMapper.readTree(outputBytes);
		final DatasetStatisticsResponse response = objectMapper.convertValue(output, DatasetStatisticsResponse.class);

		// For each column, update the metadata with the statistics
		dataset
			.getColumns()
			.stream()
			.filter(Objects::nonNull)
			.forEach(column -> {
				final JsonNode metadata = objectMapper.createObjectNode();
				final NumericColumnStats numericStats = response.getNumericColumns().get(column.getName());
				final NonNumericColumnStats nonNumericStats = response.getNonNumericColumns().get(column.getName());

				if (numericStats != null) {
					column.setDataType(DatasetColumn.mapDataType(numericStats.getDataType()));
					try {
						((ObjectNode) metadata).put("stats", objectMapper.writeValueAsString(numericStats));
					} catch (JsonProcessingException e) {
						log.error("Error serializing statistics for column {}", column.getName(), e);
					}
				} else if (nonNumericStats != null) {
					column.setDataType(DatasetColumn.mapDataType(nonNumericStats.getDataType()));
					try {
						((ObjectNode) metadata).put("stats", objectMapper.writeValueAsString(nonNumericStats));
					} catch (JsonProcessingException e) {
						log.error("Error serializing statistics for column {}", column.getName(), e);
					}
				}

				column.updateMetadata(metadata);
			});
	}
}
