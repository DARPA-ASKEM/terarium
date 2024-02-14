package software.uncharted.terarium.hmiserver.controller.climatedata;


import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import software.uncharted.terarium.hmiserver.models.funman.FunmanPostQueriesRequest;
import software.uncharted.terarium.hmiserver.proxies.climatedata.ClimateDataProxy;
import software.uncharted.terarium.hmiserver.proxies.funman.FunmanProxy;
import software.uncharted.terarium.hmiserver.security.Roles;

import java.util.UUID;

@RestController
@RequestMapping("/climatedata/queries")
public class ClimateDataController {

	@Autowired
	private ClimateDataProxy climateDataProxy;

	@GetMapping("/status/{uuid}")
	@Secured(Roles.USER)
	public ResponseEntity<JsonNode> status(@PathVariable UUID uuid) {
		ResponseEntity<JsonNode> response = climateDataProxy.status(uuid.toString());

		return ResponseEntity.ok(response.getBody());
	}

	@GetMapping("/search-esgf")
	@Secured(Roles.USER)
	public ResponseEntity<JsonNode> searchEsgf(@RequestParam("query") String query) {
		ResponseEntity<JsonNode> response = climateDataProxy.searchEsgf(query);

		return ResponseEntity.ok(response.getBody());
	}

	@GetMapping("/preview-esgf/{datasetId}")
	@Secured(Roles.USER)
	public ResponseEntity<JsonNode> previewEsgf(@PathVariable UUID datasetId,
																							@RequestParam(value = "variable-id", required = false) final String variableId,
																							@RequestParam(value = "timestamps", required = false) final String timestamps,
																							@RequestParam(value = "time-index", required = false) final String timeIndex
	) {
		ResponseEntity<JsonNode> response = climateDataProxy.previewEsgf(datasetId.toString(), variableId, timestamps, timeIndex);

		return ResponseEntity.ok(response.getBody());
	}

	@GetMapping("/subset-esgf/{datasetId}")
	@Secured(Roles.USER)
	public ResponseEntity<JsonNode> subsetEsgf(@PathVariable UUID datasetId,
																						 @RequestParam(value = "parent-dataset-id", required = false) final UUID parentDatasetId,
																						 @RequestParam(value = "timestamps", required = false) final String timestamps,
																						 @RequestParam(value = "envelope", required = false) final String envelope,
																						 @RequestParam(value = "thin-factor", required = false) final String thinFactor
	) {
		ResponseEntity<JsonNode> response = climateDataProxy.subsetEsgf(datasetId.toString(), parentDatasetId.toString(), timestamps, envelope, thinFactor);

		return ResponseEntity.ok(response.getBody());
	}

	@GetMapping("/fetch-esgf/{datasetId}")
	@Secured(Roles.USER)
	public ResponseEntity<JsonNode> fetchEsgf(@PathVariable UUID datasetId) {
		ResponseEntity<JsonNode> response = climateDataProxy.fetchEsgf(datasetId.toString());

		return ResponseEntity.ok(response.getBody());
	}
}
