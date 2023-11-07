package software.uncharted.terarium.hmiserver.controller.dataservice;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import software.uncharted.terarium.hmiserver.controller.SnakeCaseController;
import software.uncharted.terarium.hmiserver.models.SimulationIntermediateResultsCiemss;
import software.uncharted.terarium.hmiserver.models.dataservice.AssetType;
import software.uncharted.terarium.hmiserver.models.dataservice.PresignedURL;
import software.uncharted.terarium.hmiserver.models.dataservice.Simulation;
import software.uncharted.terarium.hmiserver.models.dataservice.dataset.Dataset;
import software.uncharted.terarium.hmiserver.proxies.dataservice.DatasetProxy;
import software.uncharted.terarium.hmiserver.proxies.dataservice.ProjectProxy;
import software.uncharted.terarium.hmiserver.proxies.dataservice.SimulationProxy;
import software.uncharted.terarium.hmiserver.security.Roles;
import software.uncharted.terarium.hmiserver.service.CurrentUserService;
import software.uncharted.terarium.hmiserver.service.SimulationEventService;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RequestMapping("/simulations")
@RestController
@Slf4j
@RequiredArgsConstructor
public class SimulationController implements SnakeCaseController {

	private final SimulationProxy simulationProxy;

	private final ProjectProxy projectProxy;

	private final DatasetProxy datasetProxy;

	private final CurrentUserService currentUserService;

	private final SimulationEventService simulationEventService;


	@PostMapping
	@Secured(Roles.USER)
	public ResponseEntity<JsonNode> createSimulation(@RequestBody final Simulation simulation) {
		return simulationProxy.createAsset(convertObjectToSnakeCaseJsonNode(simulation));
	}

	@GetMapping("/{id}")
	@Secured(Roles.USER)
	public ResponseEntity<Simulation> getSimulation(
		@PathVariable("id") final String id
	) {
		return ResponseEntity.ok(simulationProxy.getAsset(id).getBody());
	}

	@PutMapping("/{id}")
	@Secured(Roles.USER)
	public ResponseEntity<JsonNode> updateSimulation(@PathVariable("id") final String id, @RequestBody final Simulation simulation) {
		return ResponseEntity.ok(simulationProxy.updateAsset(id, convertObjectToSnakeCaseJsonNode(simulation)).getBody());
	}

	@DeleteMapping("/{id}")
	@Secured(Roles.USER)
	public String deleteSimulation(@PathVariable("id") final String id) {
		return ResponseEntity.ok(simulationProxy.deleteAsset(id).getBody()).toString();
	}


	@GetMapping("/{id}/result")
	@Secured(Roles.USER)
	public ResponseEntity<String> getSimulation(
		@PathVariable("id") final String id,
		@RequestParam("filename") final String filename
	) throws Exception {
		CloseableHttpClient httpclient = HttpClients.custom()
			.disableRedirectHandling()
			.build();

		final PresignedURL presignedURL = simulationProxy.getDownloadUrl(id, filename).getBody();
		if (presignedURL == null) {
			log.error("Failed to get presigned URL for simulation {} result", id);
			return ResponseEntity.internalServerError().build();
		}
		final HttpGet get = new HttpGet(presignedURL.getUrl());
		final HttpResponse response = httpclient.execute(get);
		final String data = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);

		return ResponseEntity.ok(data);
	}

	/**
	 * Creates a new dataset from a simulation result, then add it to a project as a Dataset.
	 *
	 * @param id        ID of the simulation to create a dataset from
	 * @param projectId ID of the project to add the dataset to
	 * @return Dataset the new dataset created
	 */
	@GetMapping("/{id}/add-result-as-dataset-to-project/{projectId}")
	@Secured(Roles.USER)
	public ResponseEntity<JsonNode> createFromSimulationResult(
		@PathVariable("id") final String id,
		@PathVariable("projectId") final String projectId,
		@RequestParam("datasetName") final String datasetName
	) {
		// Duplicate the simulation results to a new dataset
		Dataset dataset = simulationProxy.copyResultsToDataset(id).getBody();

		if (dataset == null) {
			log.error("Failed to create dataset from simulation {} result", id);
			return ResponseEntity.internalServerError().build();
		}

		if (datasetName != null) {
			try {
				// updateAsset actually makes a PUT request, hence why we have to fetch the whole dataset
				dataset = datasetProxy.getAsset(dataset.getId()).getBody();
				dataset.setName(datasetName);
				datasetProxy.updateAsset(dataset.getId(), convertObjectToSnakeCaseJsonNode(dataset));
			} catch (Exception e) {
				log.error("Failed to update dataset {} name", dataset.getId(), e);
				return ResponseEntity.internalServerError().build();
			}
		}


		// Add the dataset to the project as an asset
		try {
			return projectProxy.createAsset(projectId, AssetType.datasets, dataset.getId());
		} catch (Exception ignored) {
			log.error("Failed to add simulation {} result as dataset to project {}", id, projectId);
			return ResponseEntity.internalServerError().build();
		}
	}

	@GetMapping("/subscribe")
	public ResponseEntity<Void> subscribe(@RequestParam("simulationIds") final List<String> simulationIds) {

		simulationEventService.subscribe(simulationIds, currentUserService.get());
		return ResponseEntity.ok().build();
	}

	@GetMapping("/unsubscribe")
	public ResponseEntity<Void> unsubscribe(@RequestParam("simulationIds") final List<String> simulationIds) {

		simulationEventService.unsubscribe(simulationIds, currentUserService.get());
		return ResponseEntity.ok().build();
	}
}
