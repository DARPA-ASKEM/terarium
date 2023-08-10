package software.uncharted.terarium.hmiserver.resources.dataservice;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.groups.MultiSelect;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.reactivestreams.Publisher;
import org.jboss.resteasy.annotations.SseElementType;
import software.uncharted.terarium.hmiserver.models.dataservice.PresignedURL;
import software.uncharted.terarium.hmiserver.models.dataservice.ResourceType;
import software.uncharted.terarium.hmiserver.models.dataservice.Simulation;
import software.uncharted.terarium.hmiserver.models.dataservice.dataset.Dataset;
import software.uncharted.terarium.hmiserver.proxies.dataservice.DatasetProxy;
import software.uncharted.terarium.hmiserver.proxies.dataservice.ProjectProxy;
import software.uncharted.terarium.hmiserver.proxies.dataservice.SimulationProxy;
import software.uncharted.terarium.hmiserver.resources.SnakeCaseResource;
import software.uncharted.terarium.hmiserver.utils.Converter;
import software.uncharted.terarium.hmiserver.models.CalibrationIntermediateResults;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.nio.charset.StandardCharsets;

@Path("/api/simulations")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Simulation REST Endpoints")
@Slf4j
public class SimulationResource implements SnakeCaseResource {

	@Inject
	@RestClient
	SimulationProxy proxy;

	@Inject
	@RestClient
	ProjectProxy projectProxy;

	@Inject
	@RestClient
	DatasetProxy datasetProxy;

	@Inject
	@Channel("simulations") Publisher<CalibrationIntermediateResults> events;

	@POST
	public Simulation createSimulation(final Simulation simulation){
		return proxy.createSimulation(Converter.convertObjectToSnakeCaseJsonNode(simulation));
	}

	@GET
	@Path("/{id}")
	public Response getSimulation(
		@PathParam("id") final String id
	) {
		return Response
			.ok(Response.Status.OK)
			.entity(proxy.getSimulation(id))
			.build();
	}

	@PUT
	@Path("/{id}")
	public Simulation updateSimulation(@PathParam("id") final String id, final Simulation simulation){
		return proxy.updateSimulation(id, simulation);
	}

	@DELETE
	@Path("/{id}")
	public String deleteSimulation(@PathParam("id") final String id){
		return proxy.deleteSimulation(id);
	}


	@GET
	@Path("/{id}/result")
	@Produces(MediaType.TEXT_PLAIN)
	public Response getSimulation(
		@PathParam("id") final String id,
		@QueryParam("filename") final String filename
	) throws Exception {
		CloseableHttpClient httpclient = HttpClients.custom()
				.disableRedirectHandling()
				.build();

		final PresignedURL presignedURL = proxy.getDownloadURL(id, filename);
		final HttpGet get = new HttpGet(presignedURL.getUrl());
		final HttpResponse response = httpclient.execute(get);
		final String data = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);

		// return response;
		return Response
			.ok(Response.Status.OK)
			.entity(data)
			.build();
	}

	/**
	 * Creates a new dataset from a simulation result, then add it to a project as a Dataset.
	 *
	 * @param id ID of the simulation to create a dataset from
	 * @param projectId ID of the project to add the dataset to
	 * @return Dataset the new dataset created
	 */
	@GET
	@Path("/{id}/add-result-as-dataset-to-project/{projectId}")
	public Response createFromSimulationResult(
		@PathParam("id") final String id,
		@PathParam("projectId") final String projectId,
		@QueryParam("datasetName") final String datasetName
	) {
		// Duplicate the simulation results to a new dataset
		final Dataset dataset = proxy.copyResultsToDataset(id);

		if(datasetName != null){
			try {
				dataset.setName(datasetName);
				JsonNode updatedDataset = convertObjectToSnakeCaseJsonNode(dataset);
				datasetProxy.updateDataset(dataset.getId(), updatedDataset);

			} catch (Exception e) {
				log.error("Failed to update dataset {} name", dataset.getId());
				return Response
					.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity("Failed to update dataset name")
					.type("text/plain")
					.build();
			}
		}


		// Add the dataset to the project as an asset
		try {
			return projectProxy.createAsset(projectId, ResourceType.Type.DATASETS.type, dataset.getId());
		} catch (Exception ignored) {
			log.error("Failed to add simulation {} result as dataset to project {}", id, projectId);
			return Response
				.status(Response.Status.INTERNAL_SERVER_ERROR)
				.entity("Failed to add simulation result as dataset to project")
				.type("text/plain")
				.build();
		}
	}

	@GET
	@Path("/{jobId}/partial-result")
	@Produces(MediaType.SERVER_SENT_EVENTS)
	@SseElementType(MediaType.APPLICATION_JSON)
	@Tag(name = "Stream partial/intermediate simulation result associated with run ID")
	public Publisher<CalibrationIntermediateResults> stream(
		@PathParam("jobId") final String jobId
	) {
		// return Multi.createFrom().publisher(events);
		return Multi.createFrom().publisher(events).select().where(event -> event.getJobId == jobId);
	}
}
