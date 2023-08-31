package software.uncharted.terarium.hmiserver.resources.dataservice;

import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import software.uncharted.terarium.hmiserver.models.dataservice.ModelConfiguration;
import software.uncharted.terarium.hmiserver.proxies.dataservice.NotebookSessionProxy;

import software.uncharted.terarium.hmiserver.utils.Converter;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/api/notebook_sessions")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Notebook Session REST Endpoints")
public class NotebookSessionResource {
	@Inject
	@RestClient
	NotebookSessionProxy proxy;

	@GET
	public Response getNotebookSessions() {
		return proxy.getNotebookSessions();
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createNotebookSession(Object config) {
		return proxy.createNotebookSession(Converter.convertObjectToSnakeCaseJsonNode(config));
	}

	@GET
	@Path("/{id}")
	public Response getNotebookSession(
		@PathParam("id") String id
	) {
		return Response
			.ok(Response.Status.OK)
			.entity(proxy.getNotebookSession(id))
			.build();
	}

	@PUT
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateNotebookSession(
		@PathParam("id") String id,
		ModelConfiguration config
	) {
		return proxy.updateNotebookSession(id, Converter.convertObjectToSnakeCaseJsonNode(config));
	}

	@DELETE
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response deleteNotebookSession(
		@PathParam("id") String id
	) {
		return proxy.deleteNotebookSession(id);
	}
}
