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
import software.uncharted.terarium.hmiserver.service.climatedata.ClimateDataService;
import software.uncharted.terarium.hmiserver.security.Roles;

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

	@GetMapping("/preview-esgf/{datasetId}")
	@Secured(Roles.USER)
	public ResponseEntity<String> previewEsgf(@PathVariable final String datasetId,
											  @RequestParam(value = "variable-id") final String variableId,
											  @RequestParam(value = "timestamps", required = false) final String timestamps,
											  @RequestParam(value = "time-index", required = false) final String timeIndex
	) {
		String png = climateDataService.getPreviewJob(datasetId, variableId, timestamps, timeIndex);
		if (png != null) {
			return ResponseEntity.ok().body(png);
		}

		final ResponseEntity<JsonNode> response = climateDataProxy.previewEsgf(datasetId, variableId, timestamps, timeIndex);

		ClimateDataResponse climateDataResponse = objectMapper.convertValue(response.getBody(), ClimateDataResponse.class);
		climateDataService.addPreviewJob(datasetId, variableId, timestamps, timeIndex, climateDataResponse.getId());

		return ResponseEntity.accepted().build();
	}

	@GetMapping("/subset-esgf/{datasetId}")
	@Secured(Roles.USER)
	public ResponseEntity<JsonNode> subsetEsgf(@PathVariable final String datasetId,
																						 @RequestParam(value = "parent-dataset-id", required = false) final UUID parentDatasetId,
																						 @RequestParam(value = "timestamps", required = false) final String timestamps,
																						 @RequestParam(value = "envelope") final String envelope,
																						 @RequestParam(value = "thin-factor", required = false) final String thinFactor
	) {
		JsonNode jsonNode = climateDataService.getSubsetJob(datasetId, envelope, timestamps, thinFactor);
		if (jsonNode != null) {
			return ResponseEntity.ok().body(jsonNode);
		}

		final ResponseEntity<JsonNode> response = climateDataProxy.subsetEsgf(datasetId.toString(), parentDatasetId.toString(), timestamps, envelope, thinFactor);

		ClimateDataResponse climateDataResponse = objectMapper.convertValue(response.getBody(), ClimateDataResponse.class);
		climateDataService.addSubsetJob(datasetId, envelope, timestamps, thinFactor, climateDataResponse.getId());

		return ResponseEntity.accepted().build();
	}

	@GetMapping("/fetch-esgf/{datasetId}")
	@Secured(Roles.USER)
	public ResponseEntity<JsonNode> fetchEsgf(@PathVariable final UUID datasetId) {
//		final ResponseEntity<JsonNode> response = climateDataProxy.fetchEsgf(datasetId.toString());

		return ResponseEntity.status(413).build();
	}
}
