package software.uncharted.terarium.hmiserver.controller.climatedata;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.Data;
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
	@ApiResponses(
		value = {
			@ApiResponse(responseCode = "200", description = "Search ESGF and get a list of datasets"),
			@ApiResponse(responseCode = "500", description = "Internal server error")
		}
	)
	public ResponseEntity<List<Dataset>> searchEsgf(@RequestParam("query") final String query) {
		try {
			final ResponseEntity<JsonNode> response = climateDataProxy.searchEsgf(query);
			if (response == null || response.getBody() == null) {
				if (response != null && response.getStatusCode().value() >= 400) {
					log.error("Search ESGF failed. Response: {}", response);
					throw new ResponseStatusException(
						HttpStatus.valueOf(response.getStatusCode().value()),
						"Search ESGF failed."
					);
				}
				log.error("Search ESGF failed.");
				throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Search ESGF failed.");
			}

			final List<Dataset> datasets = new ArrayList<>();

			response
				.getBody()
				.get("results")
				.forEach(result -> {
					final Dataset dataset = new Dataset();
					dataset.setName(result.get("metadata").get("title").asText());
					dataset.setEsgfId(result.get("metadata").get("id").asText());
					dataset.setMetadata(result.get("metadata"));
					datasets.add(dataset);
				});

			return ResponseEntity.ok(datasets);
		} catch (final FeignException.FeignClientException e) {
			final String error = "Unable to search ESGF";
			final int status = e.status() >= 400 ? e.status() : 500;
			log.error(error, e);
			throw new ResponseStatusException(org.springframework.http.HttpStatus.valueOf(status), error);
		} catch (final Exception e) {
			if (e instanceof ResponseStatusException) {
				throw e;
			}
			final String error = "Unable to search ESGF";
			log.error(error, e);
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, error);
		}
	}

	/**
	 * @param esgfId The id of the ESGF (in the form `CMIP6.CMIP.NCAR.CESM2.historical.r11i1p1f1.CFday.ua.gn.v20190514`)
	 * @param variableId The variable to preview (ie `ua`)
	 * @param timestamps Leave blank to generate for all times
	 * @param timeIndex Leave blank to generate for all times
	 * @return
	 */
	@GetMapping("/preview-esgf/{esgfId}")
	@Secured(Roles.USER)
	@Operation(summary = "Generates a PNG for a given ESGF id")
	@ApiResponses(
		value = {
			@ApiResponse(responseCode = "200", description = "URL for the generated PNG", content = @Content),
			@ApiResponse(
				responseCode = "202",
				description = "A PNG generation request has been accepted and is being worked on",
				content = @Content
			),
			@ApiResponse(responseCode = "500", description = "There was an issue generating the PNG", content = @Content)
		}
	)
	public ResponseEntity<String> previewEsgf(
		@PathVariable final String esgfId,
		@RequestParam(value = "variable-id", required = false) final String variableId,
		@RequestParam(value = "timestamps", required = false) final String timestamps,
		@RequestParam(value = "time-index", required = false) final String timeIndex
	) {
		try {
			final String previewResponse = climateDataService.getPreview(esgfId, variableId, timestamps, timeIndex);
			if (previewResponse != null) {
				return ResponseEntity.ok(previewResponse);
			}
			final boolean successfulFetch = climateDataService.fetchPreview(esgfId, variableId, timestamps, timeIndex);
			if (successfulFetch) {
				return ResponseEntity.accepted().build();
			}
		} catch (final Exception e) {
			log.error("Error getting preview", e);
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error getting preview");
		}

		final ResponseEntity<JsonNode> response;
		try {
			response = climateDataProxy.previewEsgf(esgfId, variableId, timestamps, timeIndex);
		} catch (final FeignException e) {
			final String error = "Unable to generate preview";
			final int status = e.status() >= 400 ? e.status() : 500;
			log.error(error, e);
			throw new ResponseStatusException(org.springframework.http.HttpStatus.valueOf(status), error);
		}

		final ClimateDataResponse climateDataResponse = objectMapper.convertValue(
			response.getBody(),
			ClimateDataResponse.class
		);
		climateDataService.addPreviewJob(esgfId, variableId, timestamps, timeIndex, climateDataResponse.getId());

		return ResponseEntity.accepted().build();
	}

	/**
	 * @param esgfId The id of the ESGF (in the form `CMIP6.CMIP.NCAR.CESM2.historical.r11i1p1f1.CFday.ua.gn.v20190514`)
	 * @param parentDatasetId Dataset id of an existing dataset which this would be a child
	 * @param envelope Geographical envelope provided as a comma-separated series of 4 degrees: lon, lon, lat, lat.
	 *     example `40,45,-80,-75`.
	 * @param timestamps String of two ISO-8601 timestamps or the terms start or end separated by commas, example
	 *     `start,2010-01-01T00:00:00`. Leave blank to generate for all times
	 * @param thinFactor Take every nth datapoint along specified fields given by thin_fields. Leave blank for all data
	 * @return
	 */
	@GetMapping("/subset-esgf/{esgfId}")
	@Secured(Roles.USER)
	@Operation(summary = "Generates a subset for a given ESGF id")
	@ApiResponses(
		value = {
			@ApiResponse(responseCode = "200", description = "DatasetId for the subset", content = @Content),
			@ApiResponse(
				responseCode = "202",
				description = "A subset generation request has been accepted and is being worked on",
				content = @Content
			),
			@ApiResponse(responseCode = "500", description = "There was an issue generating the subset", content = @Content)
		}
	)
	public ResponseEntity<String> subsetEsgf(
		@PathVariable final String esgfId,
		@RequestParam(value = "parent-dataset-id") final String parentDatasetId,
		@RequestParam(value = "envelope") final String envelope,
		@RequestParam(value = "timestamps", required = false) final String timestamps,
		@RequestParam(value = "thin-factor", required = false) final String thinFactor
	) {
		final ResponseEntity<String> subsetResponse = climateDataService.getSubset(
			esgfId,
			envelope,
			timestamps,
			thinFactor
		);
		if (subsetResponse != null) {
			return subsetResponse;
		}
		final ResponseEntity<JsonNode> response;
		try {
			response = climateDataProxy.subsetEsgf(esgfId, parentDatasetId, timestamps, envelope, thinFactor);
		} catch (final FeignException e) {
			final String error = "Unable to generate subset";
			final int status = e.status() >= 400 ? e.status() : 500;
			log.error(error, e);
			throw new ResponseStatusException(org.springframework.http.HttpStatus.valueOf(status), error);
		}

		final ClimateDataResponse climateDataResponse = objectMapper.convertValue(
			response.getBody(),
			ClimateDataResponse.class
		);
		climateDataService.addSubsetJob(esgfId, envelope, timestamps, thinFactor, climateDataResponse.getId());

		return ResponseEntity.accepted().build();
	}

	@GetMapping("/fetch-esgf/{esgfId}")
	@Secured(Roles.USER)
	@ApiResponses(
		value = {
			@ApiResponse(responseCode = "200", description = "Fetch ESGF dataset"),
			@ApiResponse(responseCode = "500", description = "Internal server error")
		}
	)
	public ResponseEntity<Dataset> fetchEsgf(@PathVariable final String esgfId) {
		try {
			final ResponseEntity<JsonNode> response = climateDataProxy.fetchEsgf(esgfId);

			if (response.getBody() == null) throw new ResponseStatusException(
				HttpStatus.INTERNAL_SERVER_ERROR,
				"Unable to fetch ESGF. Response body was null"
			);

			String name = esgfId;
			if (response.getBody().get("dataset") != null) {
				name = response.getBody().get("dataset").asText();
			}

			final Dataset dataset = new Dataset();
			dataset.setEsgfId(esgfId);
			dataset.setName(name);
			dataset.setMetadata(response.getBody().get("metadata"));
			if (response.getBody().get("metadata").get("title") != null) {
				final String filename = response.getBody().get("metadata").get("title").asText();
				dataset.setFileNames(Collections.singletonList(filename));
			}
			if (response.getBody().get("urls") != null) {
				final ClimateDataResponseURLS urls = objectMapper.convertValue(
					response.getBody(),
					ClimateDataResponseURLS.class
				);
				dataset.setDatasetUrls(new ArrayList<>());
				if (urls.getUrls() != null && !urls.getUrls().isEmpty()) {
					for (final ClimateDataResponseURL url : urls.getUrls()) {
						if (url.getHttp() != null && !url.getHttp().isEmpty()) {
							dataset.getDatasetUrls().addAll(url.getHttp());
						}
					}
				}
			}

			return ResponseEntity.ok(dataset);
		} catch (final ResponseStatusException e) {
			throw e;
		} catch (final FeignException e) {
			final String error = "Unable to fetch ESGF";
			final int status = e.status() >= 400 ? e.status() : 500;
			log.error(error, e);
			throw new ResponseStatusException(org.springframework.http.HttpStatus.valueOf(status), error);
		} catch (final Exception e) {
			final String error = "Unable to fetch ESGF";
			log.error(error, e);
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, error);
		}
	}

	@Data
	public static class ClimateDataResponseURLS {

		private List<ClimateDataResponseURL> urls;
	}

	@Data
	static class ClimateDataResponseURL {

		private List<String> http;
		private List<String> opendap; // needed?
	}
}
