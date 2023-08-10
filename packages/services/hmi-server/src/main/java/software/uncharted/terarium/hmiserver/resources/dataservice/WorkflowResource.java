package software.uncharted.terarium.hmiserver.resources.dataservice;

import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import software.uncharted.terarium.hmiserver.models.dataservice.Workflow;
import software.uncharted.terarium.hmiserver.proxies.dataservice.WorkflowProxy;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.Map;

@Path("/api/workflows")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Workflow REST Endpoints")
public class WorkflowResource {
	@Inject
	@RestClient
	WorkflowProxy proxy;

	@GET
	public Response getWorkflows() {
		return proxy.getWorkflows();
	}

	@POST
	public Response createWorkflow(Workflow item) {
		return proxy.createWorkflow(item);
	}

	@PUT
	@Path("/{id}")
	public Response updateWorkflow(
		@PathParam("id") String id,
		Workflow workflow
	) {
		return proxy.updateWorkflow(id, workflow);
	}

	@GET
	@Path("/{id}")
	public Response getWorkflows(
		@PathParam("id") String id
	) {
		return proxy.getWorkflowById(id);
	}
}

