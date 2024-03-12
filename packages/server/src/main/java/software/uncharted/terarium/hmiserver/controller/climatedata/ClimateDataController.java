package software.uncharted.terarium.hmiserver.controller.climatedata;


import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import software.uncharted.terarium.hmiserver.models.dataservice.dataset.Dataset;
import software.uncharted.terarium.hmiserver.proxies.climatedata.ClimateDataProxy;
import software.uncharted.terarium.hmiserver.security.Roles;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/climatedata/queries")
@RequiredArgsConstructor
public class ClimateDataController {

	private final ClimateDataProxy climateDataProxy;

	@GetMapping("/status/{uuid}")
	@Secured(Roles.USER)
	public ResponseEntity<JsonNode> status(@PathVariable final UUID uuid) {
		final ResponseEntity<JsonNode> response = climateDataProxy.status(uuid.toString());

		return ResponseEntity.ok(response.getBody());
	}

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
	public ResponseEntity<JsonNode> previewEsgf(@PathVariable final UUID datasetId,
																							@RequestParam(value = "variable-id", required = false) final String variableId,
																							@RequestParam(value = "timestamps", required = false) final String timestamps,
																							@RequestParam(value = "time-index", required = false) final String timeIndex
	) {
		final ResponseEntity<JsonNode> response = climateDataProxy.previewEsgf(datasetId.toString(), variableId, timestamps, timeIndex);

		return ResponseEntity.ok(response.getBody());
	}

	@GetMapping("/subset-esgf/{datasetId}")
	@Secured(Roles.USER)
	public ResponseEntity<JsonNode> subsetEsgf(@PathVariable final UUID datasetId,
																						 @RequestParam(value = "parent-dataset-id", required = false) final UUID parentDatasetId,
																						 @RequestParam(value = "timestamps", required = false) final String timestamps,
																						 @RequestParam(value = "envelope", required = false) final String envelope,
																						 @RequestParam(value = "thin-factor", required = false) final String thinFactor
	) {
		final ResponseEntity<JsonNode> response = climateDataProxy.subsetEsgf(datasetId.toString(), parentDatasetId.toString(), timestamps, envelope, thinFactor);

		return ResponseEntity.ok(response.getBody());
	}

	@GetMapping("/fetch-esgf/{datasetId}")
	@Secured(Roles.USER)
	public ResponseEntity<JsonNode> fetchEsgf(@PathVariable final UUID datasetId) {
		final ResponseEntity<JsonNode> response = climateDataProxy.fetchEsgf(datasetId.toString());

		return ResponseEntity.ok(response.getBody());
	}
}
