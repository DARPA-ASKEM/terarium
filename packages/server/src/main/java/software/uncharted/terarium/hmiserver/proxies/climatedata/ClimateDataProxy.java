package software.uncharted.terarium.hmiserver.proxies.climatedata;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "climatedata-api", url = "${climate-data-service.url}")
public interface ClimateDataProxy {
	@GetMapping("/status/{uuid}")
	ResponseEntity<JsonNode> status(@PathVariable("uuid") String uuid);

	@GetMapping("/search/esgf")
	ResponseEntity<JsonNode> searchEsgf(@RequestParam("query") final String query);

	@GetMapping("/preview/esgf")
	ResponseEntity<JsonNode> previewEsgf(
		@RequestParam("dataset_id") final String datasetId,
		@RequestParam(value = "variable_id", required = false) final String variableId,
		@RequestParam(value = "timestamps", required = false) final String timestamps,
		@RequestParam(value = "time_index", required = false) final String timeIndex
	);

	@GetMapping("/subset/esgf")
	ResponseEntity<JsonNode> subsetEsgf(
		@RequestParam("dataset_id") final String datasetId,
		@RequestParam(value = "parent_id", required = false) final String parentId,
		@RequestParam(value = "timestamps", required = false) final String timestamps,
		@RequestParam(value = "envelope", required = false) final String envelope,
		@RequestParam(value = "thin_factor", required = false) final String thinFactor
	);

	@GetMapping("/fetch/esgf")
	ResponseEntity<JsonNode> fetchEsgf(@RequestParam("dataset_id") final String datasetId);
}
