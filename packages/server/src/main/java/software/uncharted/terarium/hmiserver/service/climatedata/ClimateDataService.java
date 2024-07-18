package software.uncharted.terarium.hmiserver.service.climatedata;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.IntNode;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import software.uncharted.terarium.hmiserver.configuration.Config;
import software.uncharted.terarium.hmiserver.models.climateData.ClimateDataPreview;
import software.uncharted.terarium.hmiserver.models.climateData.ClimateDataPreviewTask;
import software.uncharted.terarium.hmiserver.models.climateData.ClimateDataResponse;
import software.uncharted.terarium.hmiserver.models.climateData.ClimateDataResultPreviews;
import software.uncharted.terarium.hmiserver.models.climateData.ClimateDataResultSubset;
import software.uncharted.terarium.hmiserver.models.climateData.ClimateDataSubset;
import software.uncharted.terarium.hmiserver.models.climateData.ClimateDataSubsetTask;
import software.uncharted.terarium.hmiserver.proxies.climatedata.ClimateDataProxy;
import software.uncharted.terarium.hmiserver.repository.climateData.ClimateDataPreviewRepository;
import software.uncharted.terarium.hmiserver.repository.climateData.ClimateDataPreviewTaskRepository;
import software.uncharted.terarium.hmiserver.repository.climateData.ClimateDataSubsetRepository;
import software.uncharted.terarium.hmiserver.repository.climateData.ClimateDataSubsetTaskRepository;
import software.uncharted.terarium.hmiserver.service.s3.S3ClientService;

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

	private static final long EXPIRATION = 60;

	@Scheduled(fixedRate = 1000 * 15) // every 15 seconds
	public void checkPreviewTaskStatus() {
		final List<ClimateDataPreviewTask> previewTasks = climateDataPreviewTaskRepository.findAll();

		for (final ClimateDataPreviewTask previewTask : previewTasks) {
			final ResponseEntity<JsonNode> response = climateDataProxy.status(previewTask.getStatusId());

			if (response.getBody() != null && response.getBody() instanceof final IntNode intNode) {
				if (intNode.intValue() >= 400) {
					log.error("Failed to extract png");
					final ClimateDataPreview preview = new ClimateDataPreview(previewTask, "Failed to extract PNG");
					climateDataPreviewRepository.save(preview);
					climateDataPreviewTaskRepository.delete(previewTask);
					continue;
				}
			}

			final ClimateDataResponse climateDataResponse = objectMapper.convertValue(
				response.getBody(),
				ClimateDataResponse.class
			);
			if (!climateDataResponse.getResult().getJobResult().isNull()) {
				final ClimateDataResultPreviews previews = objectMapper.convertValue(
					climateDataResponse.getResult().getJobResult(),
					ClimateDataResultPreviews.class
				);
				if (previews != null && previews.getPreviews() != null && previews.getPreviews().size() > 0) {
					try {
						final ClimateDataResultPreviews.Preview preview = previews.getPreviews().get(0);
						final int index = preview.getImage().indexOf(',');
						if (index > -1 && index + 1 < preview.getImage().length()) {
							final String pngBase64 = preview.getImage().substring(index + 1);
							final byte[] pngBytes = Base64.getDecoder().decode(pngBase64);

							final String bucket = config.getFileStorageS3BucketName();
							final String key = getPreviewFilename(previewTask.getEsgfId(), previewTask.getVariableId());

							s3ClientService.getS3Service().putObject(bucket, key, pngBytes);

							final ClimateDataPreview climateDataPreview = new ClimateDataPreview(previewTask);

							climateDataPreviewRepository.save(climateDataPreview);
						}
					} catch (final Exception e) {
						log.error("Failed to extract png", e);
						final ClimateDataPreview preview = new ClimateDataPreview(
							previewTask,
							"Failed to extract PNG from Result: " + e.getMessage()
						);
						climateDataPreviewRepository.save(preview);

						climateDataPreviewTaskRepository.delete(previewTask);
					}
				} else {
					log.error("Failed to extract png");
					final ClimateDataPreview preview = new ClimateDataPreview(
						previewTask,
						"Failed to extract PNG from Result: " + climateDataResponse.getResult().getJobResult()
					);
					climateDataPreviewRepository.save(preview);

					climateDataPreviewTaskRepository.delete(previewTask);
				}

				climateDataPreviewTaskRepository.delete(previewTask);
			}
			if (!climateDataResponse.getResult().getJobError().isNull()) {
				final ClimateDataPreview preview = new ClimateDataPreview(
					previewTask,
					climateDataResponse.getResult().getJobError()
				);
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
			final ClimateDataResponse climateDataResponse = objectMapper.convertValue(
				response.getBody(),
				ClimateDataResponse.class
			);
			if (!climateDataResponse.getResult().getJobResult().isNull()) {
				try {
					final ClimateDataResultSubset result = objectMapper.convertValue(
						climateDataResponse.getResult().getJobResult(),
						ClimateDataResultSubset.class
					);
					if (result.getStatus().equals("failed")) {
						log.error("Failed to extract subset");
						final ClimateDataSubset subset = new ClimateDataSubset(
							subsetTask,
							"Failed to get subset : " + result.getError()
						);
						climateDataSubsetRepository.save(subset);
					} else if (result.getStatus().equals("ok")) {
						final ClimateDataSubset subset = new ClimateDataSubset(subsetTask, result.getDatasetId());

						climateDataSubsetRepository.save(subset);
					} else {
						log.error("Unexpected status extract subset: " + climateDataResponse.getResult().getJobResult());
						final ClimateDataSubset subset = new ClimateDataSubset(
							subsetTask,
							"Unexpected error during subset operation"
						);
						climateDataSubsetRepository.save(subset);
					}

					climateDataSubsetTaskRepository.delete(subsetTask);
				} catch (final Exception e) {
					log.error("Failed to extract subset");
					final ClimateDataSubset subset = new ClimateDataSubset(
						subsetTask,
						"Failed to extract subset from Result: " + climateDataResponse.getResult().getJobResult()
					);
					climateDataSubsetRepository.save(subset);

					climateDataSubsetTaskRepository.delete(subsetTask);
				}
			}
			if (!climateDataResponse.getResult().getJobError().isNull()) {
				final ClimateDataSubset subset = new ClimateDataSubset(
					subsetTask,
					climateDataResponse.getResult().getJobError()
				);
				climateDataSubsetRepository.save(subset);

				climateDataSubsetTaskRepository.delete(subsetTask);
			}
		}
	}

	private String getPreviewFilename(final String esgfId, final String variableId) {
		return String.join("/", config.getImagePath(), String.join("-", "preview", esgfId, variableId));
	}

	public ClimateDataPreviewRepository getClimateDataPreviewRepository() {
		return climateDataPreviewRepository;
	}

	public void addPreviewJob(
		final String esgfId,
		final String variableId,
		final String timestamps,
		final String timeIndex,
		final String statusId
	) {
		final ClimateDataPreviewTask task = new ClimateDataPreviewTask(statusId, esgfId, variableId, timestamps, timeIndex);
		climateDataPreviewTaskRepository.save(task);
	}

	public String getPreview(
		final String esgfId,
		final String variableId,
		final String timestamps,
		final String timeIndex
	) throws Exception {
		final List<ClimateDataPreview> previews =
			climateDataPreviewRepository.findByEsgfIdAndVariableIdAndTimestampsAndTimeIndex(
				esgfId,
				variableId,
				timestamps,
				timeIndex
			);
		if (previews != null && !previews.isEmpty()) {
			ClimateDataPreview preview = previews.get(0);
			// find successful preview
			for (final ClimateDataPreview p : previews) {
				if (p.getError() == null) {
					preview = p;
				}
			}
			if (preview.getError() != null) {
				throw new Exception(preview.getError());
			}
			final String filename = getPreviewFilename(preview.getEsgfId(), preview.getVariableId());
			final Optional<String> url = s3ClientService
				.getS3Service()
				.getS3PreSignedGetUrl(config.getFileStorageS3BucketName(), filename, EXPIRATION);
			if (url.isPresent()) {
				return url.get();
			} else {
				log.debug("Image claims to be present and yet isn't - re-requesting preview generation");
				climateDataPreviewRepository.delete(preview);
			}
		}

		return null;
	}

	public boolean fetchPreview(
		final String esgfId,
		final String variableId,
		final String timestamps,
		final String timeIndex
	) {
		final ClimateDataPreviewTask task =
			climateDataPreviewTaskRepository.findByEsgfIdAndVariableIdAndTimestampsAndTimeIndex(
				esgfId,
				variableId,
				timestamps,
				timeIndex
			);
		return task != null;
	}

	public void addSubsetJob(
		final String esgfId,
		final String envelope,
		final String timestamps,
		final String thinFactor,
		final String statusId
	) {
		final ClimateDataSubsetTask task = new ClimateDataSubsetTask(statusId, esgfId, envelope, timestamps, thinFactor);
		climateDataSubsetTaskRepository.save(task);
	}

	public ResponseEntity<String> getSubset(
		final String esgfId,
		final String envelope,
		final String timestamps,
		final String thinFactor
	) {
		final List<ClimateDataSubset> subsets =
			climateDataSubsetRepository.findByEsgfIdAndEnvelopeAndTimestampsAndThinFactor(
				esgfId,
				envelope,
				timestamps,
				thinFactor
			);
		if (subsets != null && subsets.size() > 0) {
			ClimateDataSubset subset = subsets.get(0);
			// find successful subset
			for (final ClimateDataSubset s : subsets) {
				if (s.getError() == null) {
					subset = s;
				}
			}
			if (subset.getError() != null) {
				return ResponseEntity.internalServerError().body(subset.getError());
			}
			return ResponseEntity.ok(subset.getDatasetId().toString());
		}
		final ClimateDataSubsetTask task =
			climateDataSubsetTaskRepository.findByEsgfIdAndEnvelopeAndTimestampsAndThinFactor(
				esgfId,
				envelope,
				timestamps,
				thinFactor
			);
		if (task != null) {
			return ResponseEntity.accepted().build();
		}
		return null;
	}
}
