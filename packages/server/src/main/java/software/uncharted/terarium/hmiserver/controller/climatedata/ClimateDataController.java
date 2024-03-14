package software.uncharted.terarium.hmiserver.controller.climatedata;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import software.uncharted.terarium.hmiserver.models.climateData.ClimateDataResponse;
import software.uncharted.terarium.hmiserver.models.dataservice.dataset.Dataset;
import software.uncharted.terarium.hmiserver.proxies.climatedata.ClimateDataProxy;
import software.uncharted.terarium.hmiserver.security.Roles;
import software.uncharted.terarium.hmiserver.service.climatedata.ClimateDataService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/climatedata/queries")
@RequiredArgsConstructor
public class ClimateDataController {

	private final ObjectMapper objectMapper;

	private final ClimateDataProxy climateDataProxy;

	private final ClimateDataService climateDataService;

	@GetMapping("/search-esgf")
	@Secured(Roles.USER)
	public ResponseEntity<List<Dataset>> searchEsgf(@RequestParam("query") final String query) {
		final ResponseEntity<JsonNode> response = climateDataProxy.searchEsgf(query);

		final List<Dataset> datasets = new ArrayList<>();

		response.getBody().get("results").forEach(result -> {
			final Dataset dataset = new Dataset();
			dataset.setName(result.get("metadata").get("title").asText());
			dataset.setMetadata(result.get("metadata"));
			datasets.add(dataset);
		});


		return ResponseEntity.ok(datasets);
	}

	@GetMapping("/preview-esgf/{esgfId}")
	@Secured(Roles.USER)
	public ResponseEntity<String> previewEsgf(@PathVariable final String esgfId,
											  @RequestParam(value = "variable-id") final String variableId,
											  @RequestParam(value = "timestamps", required = false) final String timestamps,
											  @RequestParam(value = "time-index", required = false) final String timeIndex
	) {
		ResponseEntity<String> previewResponse = climateDataService.getPreview(esgfId, variableId, timestamps, timeIndex);
		if (previewResponse != null) {
			return previewResponse;
		}

		final ResponseEntity<JsonNode> response = climateDataProxy.previewEsgf(esgfId, variableId, timestamps, timeIndex);

		final ClimateDataResponse climateDataResponse = objectMapper.convertValue(response.getBody(), ClimateDataResponse.class);
		climateDataService.addPreviewJob(esgfId, variableId, timestamps, timeIndex, climateDataResponse.getId());

		return ResponseEntity.accepted().build();
	}

	@GetMapping("/subset-esgf/{esgfId}")
	@Secured(Roles.USER)
	public ResponseEntity<String> subsetEsgf(@PathVariable final String esgfId,
																						 @RequestParam(value = "parent-dataset-id", required = true) final String parentDatasetId,
																						 @RequestParam(value = "timestamps", required = false) final String timestamps,
																						 @RequestParam(value = "envelope", required = true) final String envelope,
																						 @RequestParam(value = "thin-factor", required = false) final String thinFactor
	) {
		ResponseEntity<String> subsetResponse = climateDataService.getSubset(esgfId, envelope, timestamps, thinFactor);
		if (subsetResponse != null) {
			return subsetResponse;
		}

		final ResponseEntity<JsonNode> response = climateDataProxy.subsetEsgf(esgfId, parentDatasetId.toString(), timestamps, envelope, thinFactor);

		final ClimateDataResponse climateDataResponse = objectMapper.convertValue(response.getBody(), ClimateDataResponse.class);
		climateDataService.addSubsetJob(esgfId, envelope, timestamps, thinFactor, climateDataResponse.getId());

		return ResponseEntity.accepted().build();
	}

	@GetMapping("/fetch-esgf/{datasetId}")
	@Secured(Roles.USER)
	public ResponseEntity<JsonNode> fetchEsgf(@PathVariable final UUID datasetId) {
//		final ResponseEntity<JsonNode> response = climateDataProxy.fetchEsgf(datasetId.toString());

		return ResponseEntity.status(413).build();
	}
}
