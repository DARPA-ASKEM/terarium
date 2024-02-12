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

@RestController
@RequestMapping("/climatedata/queries")
public class ClimateDataController {

	@Autowired
	private ClimateDataProxy climateDataProxy;

	@GetMapping("/status/{uuid}")
	@Secured(Roles.USER)
	public ResponseEntity<JsonNode> status(@PathVariable String uuid) {
		ResponseEntity<JsonNode> response = climateDataProxy.status(uuid);

		return ResponseEntity.ok(response.getBody());
	}

	@GetMapping("/search-esgf")
	@Secured(Roles.USER)
	public ResponseEntity<JsonNode> searchEsgf(@RequestParam("query-id") String queryId) {
		ResponseEntity<JsonNode> response = climateDataProxy.searchEsgf(queryId);

		return ResponseEntity.ok(response.getBody());
	}

	@GetMapping("/preview-esgf/{datasetId}")
	@Secured(Roles.USER)
	public ResponseEntity<JsonNode> previewEsgf(@PathVariable String datasetId) {
		ResponseEntity<JsonNode> response = climateDataProxy.previewEsgf(datasetId);

		return ResponseEntity.ok(response.getBody());
	}

	@GetMapping("/subset-esgf/{datasetId}")
	@Secured(Roles.USER)
	public ResponseEntity<JsonNode> subsetEsgf(@PathVariable String datasetId,
																						 @RequestParam(value = "parent_dataset_id", required = false) final String parentDatasetId,
																						 @RequestParam(value = "timestamps", required = false) final String timestamps,
																						 @RequestParam(value = "envelope", required = false) final String envelope,
																						 @RequestParam(value = "thin_factor", required = false) final String thinFactor
	) {
		ResponseEntity<JsonNode> response = climateDataProxy.subsetEsgf(datasetId, parentDatasetId, timestamps, envelope, thinFactor);

		return ResponseEntity.ok(response.getBody());
	}

	@GetMapping("/fetch-esgf/{datasetId}")
	@Secured(Roles.USER)
	public ResponseEntity<JsonNode> fetchEsgf(@PathVariable String datasetId) {
		ResponseEntity<JsonNode> response = climateDataProxy.fetchEsgf(datasetId);

		return ResponseEntity.ok(response.getBody());
	}
}
