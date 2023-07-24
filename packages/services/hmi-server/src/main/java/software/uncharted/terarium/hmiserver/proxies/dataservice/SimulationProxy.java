package software.uncharted.terarium.hmiserver.proxies.dataservice;

import com.fasterxml.jackson.databind.JsonNode;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import software.uncharted.terarium.hmiserver.annotations.LogRestClientTime;
import software.uncharted.terarium.hmiserver.models.dataservice.Simulation;
import software.uncharted.terarium.hmiserver.models.dataservice.PresignedURL;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@RegisterRestClient(configKey = "data-service")
@Path("/simulations")
@Produces(MediaType.APPLICATION_JSON)
public interface SimulationProxy {

	@GET
	@Path("/{id}")
	@LogRestClientTime
	Simulation getSimulation(
		@PathParam("id") String id
	);

	@POST
	@LogRestClientTime
	Simulation createSimulation(
		JsonNode simulation
	);

	@PATCH
	@Path("/{id}")
	@LogRestClientTime
	Simulation updateSimulation(
		@PathParam("id") String id,
		Simulation simulation
	);

	@DELETE
	@Path("/{id}")
	@LogRestClientTime
	String deleteSimulation(
		@PathParam("id") String id
	);

	@GET
	@Path("/{id}/upload-url")
	@LogRestClientTime
	PresignedURL getUploadURL(
		@PathParam("id") String id,
		@QueryParam("filename") String filename
	);

	@GET
	@Path("/{id}/download-url")
	@LogRestClientTime
	PresignedURL getDownloadURL(
		@PathParam("id") String id,
		@QueryParam("filename") String filename
	);

	@GET
	@Path("/{id}/copy_results")
	@LogRestClientTime
	JsonNode copyResultsToDataset(
		@PathParam("id") String id
	);
}
