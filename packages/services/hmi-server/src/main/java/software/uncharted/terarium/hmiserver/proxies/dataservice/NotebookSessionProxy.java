package software.uncharted.terarium.hmiserver.proxies.dataservice;

import com.fasterxml.jackson.databind.JsonNode;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import software.uncharted.terarium.hmiserver.annotations.LogRestClientTime;
import software.uncharted.terarium.hmiserver.exceptions.HmiResponseExceptionMapper;
import software.uncharted.terarium.hmiserver.models.dataservice.NotebookSession;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@RegisterRestClient(configKey = "data-service")
@Path("/notebook_sessions")
@Produces(MediaType.APPLICATION_JSON)
@RegisterProvider(HmiResponseExceptionMapper.class)
public interface NotebookSessionProxy {
	@GET
	@LogRestClientTime
	Response getNotebookSessions();

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@LogRestClientTime
	Response createNotebookSession(
		JsonNode config
	);

	@GET
	@Path("/{id}")
	@LogRestClientTime
	NotebookSession getNotebookSession(
		@PathParam("id") String id
	);

	@PUT
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@LogRestClientTime
	Response updateNotebookSession(
		@PathParam("id") String id,
		JsonNode config
	);

	@DELETE
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@LogRestClientTime
	Response deleteNotebookSession(
		@PathParam("id") String id
	);
}
