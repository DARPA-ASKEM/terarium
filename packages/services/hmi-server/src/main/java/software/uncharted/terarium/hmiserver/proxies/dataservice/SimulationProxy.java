package software.uncharted.terarium.hmiserver.proxies.dataservice;

import com.fasterxml.jackson.databind.JsonNode;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import software.uncharted.terarium.hmiserver.models.dataservice.Simulation;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;

@RegisterRestClient(configKey = "data-service")
@Path("/simulations")
@Produces(MediaType.APPLICATION_JSON)
public interface SimulationProxy {

	@GET
	@Path("/{id}")
	Simulation getSimulation(
		@PathParam("id") String id
	);

	@POST
	Simulation createSimulation(
		JsonNode simulation
	);

	@PATCH
	@Path("/{id}")
	Simulation updateSimulation(
		@PathParam("id") String id,
		Simulation simulation
	);

	@DELETE
	@Path("/{id}")
	String deleteSimulation(
		@PathParam("id") String id
	);

	@GET
	@Path("/{id}/upload-url")
	String getUploadURL(
		@PathParam("id") String id,
		@QueryParam("filename" String filename
	);

	@GET
	@Path("/{id}/download-url")
	String getDownloadURL(
		@PathParam("id") String id,
		@QueryParam("filename" String filename
	);
}
