package software.uncharted.terarium.hmiserver.controller.dataservice;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import software.uncharted.terarium.hmiserver.controller.SnakeCaseController;
import software.uncharted.terarium.hmiserver.models.SimulationIntermediateResultsCiemss;
import software.uncharted.terarium.hmiserver.models.dataservice.AssetType;
import software.uncharted.terarium.hmiserver.models.dataservice.PresignedURL;
import software.uncharted.terarium.hmiserver.models.dataservice.Simulation;
import software.uncharted.terarium.hmiserver.models.dataservice.dataset.Dataset;
import software.uncharted.terarium.hmiserver.proxies.dataservice.DatasetProxy;
import software.uncharted.terarium.hmiserver.proxies.dataservice.ProjectProxy;
import software.uncharted.terarium.hmiserver.proxies.dataservice.SimulationProxy;

import java.nio.charset.StandardCharsets;

@RequestMapping("/simulations")
@RestController
@Slf4j
public class SimulationController implements SnakeCaseController {

	@Autowired
	SimulationProxy simulationProxy;

	@Autowired
	ProjectProxy projectProxy;

	@Autowired
	DatasetProxy datasetProxy;

	//TODO: https://github.com/DARPA-ASKEM/Terarium/issues/1757

	//TODO: @tszendrey is taking these on or becoming a bartender trying.
	/*@Inject
	@Channel("simulationStatus") Publisher<byte[]> simulationStatusStream;

	@Broadcast
	@Channel("simulationStatus")
	Emitter<SimulationIntermediateResultsCiemss> simulationStatusEmitter;

	@Inject
	@Channel("scimlQueue") Publisher<JsonObject> scimlQueueStream;*/

	@PostMapping
	public ResponseEntity<JsonNode> createSimulation(@RequestBody final Simulation simulation) {
		return simulationProxy.createAsset(convertObjectToSnakeCaseJsonNode(simulation));
	}

	@GetMapping("/{id}")
	public ResponseEntity<Simulation> getSimulation(
		@PathVariable("id") final String id
	) {
		return ResponseEntity.ok(simulationProxy.getAsset(id).getBody());
	}

	@PutMapping("/{id}")
	public ResponseEntity<JsonNode> updateSimulation(@PathVariable("id") final String id, @RequestBody final Simulation simulation) {
		return ResponseEntity.ok(simulationProxy.updateAsset(id, convertObjectToSnakeCaseJsonNode(simulation)).getBody());
	}

	@DeleteMapping("/{id}")
	public String deleteSimulation(@PathVariable("id") final String id) {
		return ResponseEntity.ok(simulationProxy.deleteAsset(id).getBody()).toString();
	}


	@GetMapping("/{id}/result")
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
	public ResponseEntity<JsonNode> createFromSimulationResult(
		@PathVariable("id") final String id,
		@PathVariable("projectId") final String projectId,
		@RequestParam("datasetName") final String datasetName
	) {
		// Duplicate the simulation results to a new dataset
		final Dataset dataset = simulationProxy.copyResultsToDataset(id).getBody();

		if (dataset == null) {
			log.error("Failed to create dataset from simulation {} result", id);
			return ResponseEntity.internalServerError().build();
		}

		if (datasetName != null) {
			try {
				dataset.setName(datasetName);
				datasetProxy.updateAsset(dataset.getId(), convertObjectToSnakeCaseJsonNode(dataset));

			} catch (Exception e) {
				log.error("Failed to update dataset {} name", dataset.getId());
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

	@GetMapping("/{jobId}/ciemss/partial-result")
	//TODO @SseElementType(MediaType.APPLICATION_JSON)
	public ResponseEntity<JsonNode> stream(
		@PathVariable("jobId") final String jobId
	) {

		// return not implemented until we have a working solution
		return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();

		/*TODO
		ObjectMapper mapper = new ObjectMapper();
		return Multi.createFrom().publisher(simulationStatusStream).filter(event -> {
			try{
				//TODO: https://github.com/DARPA-ASKEM/Terarium/issues/1757
				String jsonString = new String(event);
				jsonString = jsonString.replace(" ","");

				SimulationIntermediateResultsCiemss interResult = mapper.readValue(jsonString, SimulationIntermediateResultsCiemss.class);

				return interResult.getJobId().equals(jobId);
			}
			catch(Exception e){
				log.error("Error occurred while trying to convert simulation-status message to type: SimulationIntermediateResultsCiemss");
				log.error(event.toString());
				log.error(e.toString());
				return false;
			}
		});*/
	}

	@GetMapping("/{jobId}/sciml/partial-result")
	//TODO @SseElementType(MediaType.APPLICATION_JSON)
	public ResponseEntity<JsonNode> scimlResult(
		@PathVariable("jobId") final String jobId
	) {

		// return not implemented until we have a working solution
		return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();

		/* TODO
		return Multi.createFrom().publisher(scimlQueueStream).filter(event -> {
			try {
				return event.getValue("id").equals(jobId);
			}
			catch(Exception e) {
				log.error("Error occured while trying to consume sciml-queue message");
				log.error(event.toString());
				log.error(e.toString());
				return false;
			}
		});*/
	}

	// When we finalize the SimulationIntermediateResults object this end point will need to be passed more parameters
	//TODO: https://github.com/DARPA-ASKEM/Terarium/issues/1757
	@PutMapping("/{jobId}/ciemss/create-partial-result")
	public ResponseEntity<JsonNode> createPartialResult(
		@PathVariable("jobId") final String jobId
	) {
		Double progress = 0.01;
		SimulationIntermediateResultsCiemss event = new SimulationIntermediateResultsCiemss();
		event.setJobId(jobId);
		event.setProgress(progress);
		//TODO simulationStatusEmitter.send(event);
		return ResponseEntity.ok().build();
	}
}
