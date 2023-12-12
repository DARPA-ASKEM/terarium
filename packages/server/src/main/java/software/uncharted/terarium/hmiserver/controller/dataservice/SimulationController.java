package software.uncharted.terarium.hmiserver.controller.dataservice;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import software.uncharted.terarium.hmiserver.models.data.simulation.Simulation;
import software.uncharted.terarium.hmiserver.models.dataservice.PresignedURL;
import software.uncharted.terarium.hmiserver.security.Roles;
import software.uncharted.terarium.hmiserver.service.CurrentUserService;
import software.uncharted.terarium.hmiserver.service.SimulationEventService;
import software.uncharted.terarium.hmiserver.service.data.SimulationService;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RequestMapping("/simulations")
@RestController
@Slf4j
@RequiredArgsConstructor
public class SimulationController {

	private final SimulationService simulationService;

	private final CurrentUserService currentUserService;

	private final SimulationEventService simulationEventService;


	@PostMapping
	@Secured(Roles.USER)
	@Operation(summary = "Create a new simulation")
	@ApiResponses(value = {
		@ApiResponse(
			responseCode = "200",
			description = "Simulation created.",
			content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = Simulation.class))
		),
		@ApiResponse(
			responseCode = "500",
			description = "There was an issue creating the simulation",
			content = @Content
		)
	})
	public ResponseEntity<Simulation> createSimulation(@RequestBody final Simulation simulation) {
		try {
			return ResponseEntity.ok(simulationService.createSimulation(simulation));
		} catch (final Exception e) {
			final String error = "Failed to create simulation.";
			log.error(error, e);
			throw new ResponseStatusException(
				org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
				error
			);
		}
	}

	@GetMapping("/{id}")
	@Secured(Roles.USER)
	@Operation(summary = "Get a simulation by ID")
	@ApiResponses(value = {
		@ApiResponse(
			responseCode = "200",
			description = "Simulation found.",
			content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = Simulation.class))
		),
		@ApiResponse(
			responseCode = "204",
			description = "There are no simulations found and no errors occurred",
			content = @Content
		),
		@ApiResponse(
			responseCode = "500",
			description = "There was an issue retrieving simulations from the data store",
			content = @Content
		)
	})
	public ResponseEntity<Simulation> getSimulation(
		@PathVariable("id") final String id
	) {
		try {
			final Simulation simulation = simulationService.getSimulation(id);
			if (simulation == null) {
				return ResponseEntity.noContent().build();
			}
			return ResponseEntity.ok(simulation);
		} catch (final Exception e) {
			final String error = String.format("Failed to get simulation %s", id);
			log.error(error, e);
			throw new ResponseStatusException(
				org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
				error
			);
		}
	}

	@PutMapping("/{id}")
	@Secured(Roles.USER)
	@Operation(summary = "Update a simulation by ID")
	@ApiResponses(value = {
		@ApiResponse(
			responseCode = "200",
			description = "Simulation updated.",
			content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = Simulation.class))
		),
		@ApiResponse(
			responseCode = "500",
			description = "There was an issue updating the simulation",
			content = @Content
		)
	})
	public ResponseEntity<Simulation> updateSimulation(@PathVariable("id") final String id, @RequestBody final Simulation simulation) {
		try {
			return ResponseEntity.ok(simulationService.updateSimulation(id, simulation));
		} catch (final Exception e) {
			final String error = String.format("Failed to update simulation %s", id);
			log.error(error, e);
			throw new ResponseStatusException(
				org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
				error
			);
		}
	}

	@DeleteMapping("/{id}")
	@Secured(Roles.USER)
	@Operation(summary = "Delete a simulation by ID")
	@ApiResponses(value = {
		@ApiResponse(
			responseCode = "200",
			description = "Simulation deleted.",
			content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE)
		),
		@ApiResponse(
			responseCode = "500",
			description = "There was an issue deleting the simulation",
			content = @Content
		)
	})
	public String deleteSimulation(@PathVariable("id") final String id) {
		try {
			simulationService.deleteSimulation(id);
			return "Simulation deleted";
		} catch (final Exception e) {
			final String error = String.format("Failed to delete simulation %s", id);
			log.error(error, e);
			throw new ResponseStatusException(
				org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
				error
			);
		}
	}


	@GetMapping("/{id}/result")
	@Secured(Roles.USER)
	public ResponseEntity<String> getSimulationResults(
		@PathVariable("id") final String id,
		@RequestParam("filename") final String filename
	) throws Exception {
		try (final CloseableHttpClient httpclient = HttpClients.custom().disableRedirectHandling().build()) {
			final PresignedURL presignedURL = simulationService.getDownloadUrl(id, filename);
			if (presignedURL == null) {
				final String error = String.format("Failed to get presigned URL for result of simulation %s", id);
				log.error(error);
				throw new ResponseStatusException(
					org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
					error
				);
			}

			final HttpGet get = new HttpGet(presignedURL.getUrl());
			final HttpResponse response = httpclient.execute(get);
			final String data = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);

			return ResponseEntity.ok(data);
		} catch (final Exception e) {
			final String error = String.format("Failed to get result of simulation %s", id);
			log.error(error, e);
			throw new ResponseStatusException(
				org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
				error
			);
		}
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
		throw new ResponseStatusException(
			HttpStatus.NOT_IMPLEMENTED,
			"Not implemented"
		);
//
//		// Duplicate the simulation results to a new dataset
//		Dataset dataset = simulationProxy.copyResultsToDataset(id).getBody();
//
//		if (dataset == null) {
//			log.error("Failed to create dataset from simulation {} result", id);
//			return ResponseEntity.internalServerError().build();
//		}
//
//		if (datasetName != null) {
//			try {
//				// updateAsset actually makes a PUT request, hence why we have to fetch the whole dataset
//				dataset = datasetProxy.getAsset(dataset.getId()).getBody();
//				dataset.setName(datasetName);
//				datasetProxy.updateAsset(dataset.getId(), convertObjectToSnakeCaseJsonNode(dataset));
//			} catch (final Exception e) {
//				log.error("Failed to update dataset {} name", dataset.getId(), e);
//				return ResponseEntity.internalServerError().build();
//			}
//		}
//
//
//		// Add the dataset to the project as an asset
//		try {
//			return projectProxy.createAsset(projectId, AssetType.datasets, dataset.getId());
//		} catch (final Exception ignored) {
//			log.error("Failed to add simulation {} result as dataset to project {}", id, projectId);
//			return ResponseEntity.internalServerError().build();
//		}
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
