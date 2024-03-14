package software.uncharted.terarium.hmiserver.controller.climatedata;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
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
@Slf4j
public class ClimateDataController {

	private final ObjectMapper objectMapper;

	private final ClimateDataProxy climateDataProxy;

	private final ClimateDataService climateDataService;

	@GetMapping("/search-esgf")
	@Secured(Roles.USER)
	public ResponseEntity<List<Dataset>> searchEsgf(@RequestParam("query") final String query) {
		try {
			final ResponseEntity<JsonNode> response = climateDataProxy.searchEsgf(query);
			if(response == null || response.getBody() == null){
				throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Search ESGF failed.");
			}

			final List<Dataset> datasets = new ArrayList<>();

			response.getBody().get("results").forEach(result -> {
				final Dataset dataset = new Dataset();
				dataset.setName(result.get("metadata").get("title").asText());
				dataset.setEsgfId(result.get("metadata").get("id").asText());
				dataset.setMetadata(result.get("metadata"));
				datasets.add(dataset);
			});


			return ResponseEntity.ok(datasets);
		} catch(final FeignException.FeignClientException e) {
			final String error = "Unable to search ESGF";
			final int status = e.status() >=400? e.status(): 500;
			log.error(error, e);
			throw new ResponseStatusException(org.springframework.http.HttpStatus.valueOf(status), error);

		}
	}

	@GetMapping("/preview-esgf/{esgfId}")
	@Secured(Roles.USER)
	public ResponseEntity<String> previewEsgf(@PathVariable final String esgfId,
											  @RequestParam(value = "variable-id") final String variableId,
											  @RequestParam(value = "timestamps", required = false) final String timestamps,
											  @RequestParam(value = "time-index", required = false) final String timeIndex
	) {
		final ResponseEntity<String> previewResponse = climateDataService.getPreview(esgfId, variableId, timestamps, timeIndex);
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
	public ResponseEntity<JsonNode> subsetEsgf(@PathVariable final String esgfId,
																						 @RequestParam(value = "parent-dataset-id", required = false) final UUID parentDatasetId,
																						 @RequestParam(value = "timestamps", required = false) final String timestamps,
																						 @RequestParam(value = "envelope") final String envelope,
																						 @RequestParam(value = "thin-factor", required = false) final String thinFactor
	) {
		final JsonNode jsonNode = ClimateDataService.getSubsetJob(esgfId, envelope, timestamps, thinFactor);
		if (jsonNode != null) {
			return ResponseEntity.ok().body(jsonNode);
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
