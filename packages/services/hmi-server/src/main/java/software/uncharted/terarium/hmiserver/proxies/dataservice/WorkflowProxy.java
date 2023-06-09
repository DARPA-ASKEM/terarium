package software.uncharted.terarium.hmiserver.proxies.dataservice;

import software.uncharted.terarium.hmiserver.models.dataservice.Workflow;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import software.uncharted.terarium.hmiserver.exceptions.HmiResponseExceptionMapper;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;

@RegisterRestClient(configKey = "data-service")
@Path("/workflows")
@Produces(MediaType.APPLICATION_JSON)
@RegisterProvider(HmiResponseExceptionMapper.class)
public interface WorkflowProxy {
	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	Response getWorkflows();

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	Response createWorkflow(
		Workflow workflow
	);

	@PUT
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	Response updateWorkflow(
		@PathParam("id") String id
		Workflow workflow
	);


	@GET
	@Path("/{id}")
	Response getWorkflowById(
		@PathParam("id") String id
	);
}
