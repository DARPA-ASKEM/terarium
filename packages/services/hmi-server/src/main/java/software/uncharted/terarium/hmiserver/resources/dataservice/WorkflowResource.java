package software.uncharted.terarium.hmiserver.resources.dataservice;

import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import software.uncharted.terarium.hmiserver.models.dataservice.Workflow;
import software.uncharted.terarium.hmiserver.proxies.dataservice.WorkflowProxy;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;

@Path("/api/workflows")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Workflow REST Endpoints")
public class WorkflowResource {
	@Inject
	@RestClient
	WorkflowProxy proxy;

	@GET
	@Path("/")
	public Response getWorkflows() {
		return proxy.getWorkflows();
	}

	@GET
	@Path("/{id}")
	public Response getWorkflows(
		@PathParam("id") String id
	) {
		return proxy.getWorkflowById(id);
	}
}

