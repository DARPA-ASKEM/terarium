package software.uncharted.terarium.hmiserver.controller.dataservice;

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
import software.uncharted.terarium.hmiserver.models.dataservice.AssetType;
import software.uncharted.terarium.hmiserver.models.dataservice.PresignedURL;
import software.uncharted.terarium.hmiserver.models.dataservice.dataset.Dataset;
import software.uncharted.terarium.hmiserver.models.dataservice.project.Project;
import software.uncharted.terarium.hmiserver.models.dataservice.project.ProjectAsset;
import software.uncharted.terarium.hmiserver.models.dataservice.simulation.Simulation;
import software.uncharted.terarium.hmiserver.security.Roles;
import software.uncharted.terarium.hmiserver.service.CurrentUserService;
import software.uncharted.terarium.hmiserver.service.SimulationEventService;
import software.uncharted.terarium.hmiserver.service.data.DatasetService;
import software.uncharted.terarium.hmiserver.service.data.ProjectAssetService;
import software.uncharted.terarium.hmiserver.service.data.ProjectService;
import software.uncharted.terarium.hmiserver.service.data.SimulationService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequestMapping("/simulations")
@RestController
@Slf4j
@RequiredArgsConstructor
public class SimulationController {

	private final SimulationService simulationService;

	private final DatasetService datasetService;

	private final CurrentUserService currentUserService;

	private final ProjectService projectService;
	private final ProjectAssetService projectAssetService;

	private final SimulationEventService simulationEventService;

	@PostMapping
	@Secured(Roles.USER)
	@Operation(summary = "Create a new simulation")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "Simulation created.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = Simulation.class))),
			@ApiResponse(responseCode = "500", description = "There was an issue creating the simulation", content = @Content)
	})
	public ResponseEntity<Simulation> createSimulation(@RequestBody final Simulation simulation) {
		try {
			return ResponseEntity.status(HttpStatus.CREATED).body(simulationService.createSimulation(simulation));
		} catch (final Exception e) {
			final String error = "Failed to create simulation.";
			log.error(error, e);
			throw new ResponseStatusException(
					org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
					error);
		}
	}

	@GetMapping("/{id}")
	@Secured(Roles.USER)
	@Operation(summary = "Get a simulation by ID")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Simulation found.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = Simulation.class))),
			@ApiResponse(responseCode = "204", description = "There are no simulations found and no errors occurred", content = @Content),
			@ApiResponse(responseCode = "500", description = "There was an issue retrieving simulations from the data store", content = @Content)
	})
	public ResponseEntity<Simulation> getSimulation(
			@PathVariable("id") final UUID id) {
		try {
			final Optional<Simulation> simulation = simulationService.getSimulation(id);
			if (simulation.isEmpty()) {
				return ResponseEntity.noContent().build();
			}
			return ResponseEntity.ok(simulation.get());
		} catch (final Exception e) {
			final String error = String.format("Failed to get simulation %s", id);
			log.error(error, e);
			throw new ResponseStatusException(
					org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
					error);
		}
	}

	@PutMapping("/{id}")
	@Secured(Roles.USER)
	@Operation(summary = "Update a simulation by ID")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Simulation updated.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = Simulation.class))),
			@ApiResponse(responseCode = "500", description = "There was an issue updating the simulation", content = @Content)
	})
	public ResponseEntity<Simulation> updateSimulation(@PathVariable("id") final UUID id,
			@RequestBody final Simulation simulation) {
		try {
			final Optional<Simulation> updated = simulationService.updateSimulation(simulation.setId(id));
			if (updated.isEmpty()) {
				return ResponseEntity.notFound().build();
			}
			return ResponseEntity.ok(updated.get());
		} catch (final Exception e) {
			final String error = String.format("Failed to update simulation %s", id);
			log.error(error, e);
			throw new ResponseStatusException(
					org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
					error);
		}
	}

	@DeleteMapping("/{id}")
	@Secured(Roles.USER)
	@Operation(summary = "Delete a simulation by ID")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Simulation deleted.", content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE)),
			@ApiResponse(responseCode = "500", description = "There was an issue deleting the simulation", content = @Content)
	})
	public String deleteSimulation(@PathVariable("id") final UUID id) {
		try {
			simulationService.deleteSimulation(id);
			return "Simulation deleted";
		} catch (final Exception e) {
			final String error = String.format("Failed to delete simulation %s", id);
			log.error(error, e);
			throw new ResponseStatusException(
					org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
					error);
		}
	}

	@GetMapping("/{id}/result")
	@Secured(Roles.USER)
	public ResponseEntity<String> getSimulationResults(
			@PathVariable("id") final UUID id,
			@RequestParam("filename") final String filename) {

		try (final CloseableHttpClient httpclient = HttpClients.custom().disableRedirectHandling().build()) {
			final PresignedURL presignedURL = simulationService.getDownloadUrl(id, filename);
			if (presignedURL == null) {
				final String error = String.format("Failed to get presigned URL for result of simulation %s", id);
				log.error(error);
				throw new ResponseStatusException(
						org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
						error);
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
					error);
		}
	}

	/**
	 * Creates a new dataset from a simulation result, then add it to a project as a
	 * Dataset.
	 *
	 * @param id        ID of the simulation to create a dataset from
	 * @param projectId ID of the project to add the dataset to
	 * @return Dataset the new dataset created
	 */
	@GetMapping("/{id}/add-result-as-dataset-to-project/{projectId}")
	@Secured(Roles.USER)
	public ResponseEntity<ProjectAsset> createFromSimulationResult(
			@PathVariable("id") final UUID id,
			@PathVariable("projectId") final UUID projectId,
			@RequestParam("datasetName") final String datasetName) {

		try {
			final Optional<Simulation> sim = simulationService.getSimulation(id);
			if (sim.isEmpty()) {
				return ResponseEntity.notFound().build();
			}

			// Duplicate the simulation results to a new dataset
			final Dataset dataset = simulationService.copySimulationResultToDataset(sim.get());
			if (dataset == null) {
				log.error("Failed to create dataset from simulation {} result", id);
				return ResponseEntity.internalServerError().build();
			}

			dataset.setName(datasetName);
			datasetService.createDataset(dataset);

			// Add the dataset to the project as an asset
			final Optional<Project> project = projectService.getProject(projectId);
			if (project.isPresent()) {
				final ProjectAsset asset = projectAssetService.createProjectAsset(project.get(), AssetType.DATASET,
						dataset.getId());
				return ResponseEntity.status(HttpStatus.CREATED).body(asset);
			} else {
				log.error("Failed to add the dataset from simulation {} result", id);
				return ResponseEntity.internalServerError().build();
			}
		} catch (final IOException e) {
			final String error = String.format("Failed to add simulation %s result as dataset to project %s", id,
					projectId);
			log.error(error, e);
			throw new ResponseStatusException(
					org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
					error);
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
