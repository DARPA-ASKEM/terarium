package software.uncharted.terarium.hmiserver.service.tasks;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.uncharted.terarium.hmiserver.models.dataservice.Summary;
import software.uncharted.terarium.hmiserver.models.task.TaskResponse;
import software.uncharted.terarium.hmiserver.service.data.SummaryService;

import java.util.Base64;
import java.util.UUID;

import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class GenerateSummaryHandler extends TaskResponseHandler {
	public static final String NAME = "gollm_task:generate_summary";

	private final SummaryService summaryService;

	@Override
	public String getName() {
		return NAME;
	}

	@Data
	public static class Properties {
		UUID previousSummaryId;
		UUID summaryId;
	}

	// @Override
	// public TaskResponse onRunning(final TaskResponse resp) {
	// 	try {
	// 		final Properties props = resp.getAdditionalProperties(Properties.class);
	// 		final Summary newSummary = new Summary();
	// 		newSummary.setPreviousSummary(props.previousSummaryId);
	// 		newSummary.setId(props.summaryId);
	// 		summaryService.createAsset(newSummary, ASSUME_WRITE_PERMISSION_ON_BEHALF_OF_USER);
	// 	} catch (final Exception e) {
	// 		log.error("Failed to create a summary", e);
	// 		throw new RuntimeException(e);
	// 	}
	// 	return resp;
	// }

	@Override
	public TaskResponse onFailure(final TaskResponse resp) {
		try {
			final Properties props = resp.getAdditionalProperties(Properties.class);
			final Summary summary = summaryService.getAsset(props.summaryId, ASSUME_WRITE_PERMISSION_ON_BEHALF_OF_USER).orElseThrow();
			summary.setGeneratedSummary("Generating AI summary has failed.");
			summaryService.updateAsset(summary, ASSUME_WRITE_PERMISSION_ON_BEHALF_OF_USER);
		} catch (final Exception e) {
			log.error("Failed to update a summary", e);
			throw new RuntimeException(e);
		}
		return resp;
	}

	@Override
	public TaskResponse onSuccess(final TaskResponse resp) {
		try {
			final Properties props = resp.getAdditionalProperties(Properties.class);
			final String output = new String(Base64.getDecoder().decode(resp.getOutput().toString()));

			final Summary summary = summaryService.getAsset(props.summaryId, ASSUME_WRITE_PERMISSION_ON_BEHALF_OF_USER).orElseThrow();
			summary.setGeneratedSummary(output);
			summaryService.updateAsset(summary, ASSUME_WRITE_PERMISSION_ON_BEHALF_OF_USER);
		} catch (final Exception e) {
			log.error("Failed to update the summary", e);
			throw new RuntimeException(e);
		}
		return resp;
	}


	// @Override
	// public TaskResponse onSuccess(final TaskResponse resp) {
	// 	try {
	// 		final Properties props = resp.getAdditionalProperties(Properties.class);
	// 		final String output = new String(Base64.getDecoder().decode(resp.getOutput().toString()));

	// 		final Summary newSummary = new Summary();
	// 		newSummary.setPreviousSummary(props.previousSummaryId);
	// 		newSummary.setId(props.summaryId);
	// 		newSummary.setGeneratedSummary(output);
	// 		summaryService.createAsset(newSummary, ASSUME_WRITE_PERMISSION_ON_BEHALF_OF_USER);
	// 	} catch (final Exception e) {
	// 		log.error("Failed to save the summary", e);
	// 	}
	// 	return resp;
	// }
}
