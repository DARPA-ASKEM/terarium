package software.uncharted.terarium.hmiserver.resources;

import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import software.uncharted.terarium.hmiserver.models.SimulationPlan;
import software.uncharted.terarium.hmiserver.models.SimulationRun;
import software.uncharted.terarium.hmiserver.proxies.SimulationPlanProxy;
import software.uncharted.terarium.hmiserver.proxies.SimulationResultProxy;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.List;

@Path("/api/simulation")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Simulation REST Endpoints")
public class SimulationResource {

	@Inject
	@RestClient
	SimulationPlanProxy planProxy;

	@Inject
	@RestClient
	SimulationResultProxy resultsProxy;

	@GET
	@Path("/plan")
	@Produces(MediaType.APPLICATION_JSON)
	@Tag(name = "Get all projects for a given user")
	public Response getSimulationPlan() {
		final List<SimulationPlan> plans = planProxy.getSimulationPlans();
		if (plans.isEmpty()) {
			return Response.noContent().build();
		}
		return Response.ok(plans).build();
	}

	@GET
	@Path("/plan/{id}")
	public Response getSimulationPlan(
		@QueryParam("id") final Long id
	) {
		final SimulationPlan entity = planProxy.getSimulationPlan(id);

		if (entity == null) {
			throw new WebApplicationException(Response.Status.NOT_FOUND);
		}
		return Response.ok(entity).build();
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createSimulationPlan(final SimulationPlan newPlan) {
		final SimulationPlan entity = planProxy.createSimulationPlan(newPlan);
		return Response.created(URI.create("api/simulation/plan/" + entity.id)).build();
	}

	@PUT
	@Path("/plan/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateSimulationPlan(@PathParam("id") final Long id, final SimulationPlan updatedPlan) {
		if (planProxy.getSimulationPlan(id) == null) {
			throw new WebApplicationException(Response.Status.NOT_FOUND);
		}

		final SimulationPlan entity = planProxy.updateSimulationPlan(id, updatedPlan);

		if (entity == null) {
			return Response.noContent().build();
		}
		return Response.ok(entity).build();
	}

	@DELETE
	@Path("/plan/{id}")
	public Response deleteSimulationPlan(@PathParam("id") final Long id) {
		if (!planProxy.deleteSimulationPlan(id)) {
			throw new WebApplicationException(Response.Status.NOT_FOUND);
		}

		return Response.ok().build();
	}

	@GET
	@Path("/result")
	@Produces(MediaType.APPLICATION_JSON)
	@Tag(name = "Get all projects for a given user")
	public Response getSimulationResults() {
		final List<SimulationRun> plans = resultsProxy.getSimulationResults();
		if (plans.isEmpty()) {
			return Response.noContent().build();
		}
		return Response.ok(plans).build();
	}

	@GET
	@Path("/result/{id}")
	public Response getSimulationResult(
		@PathParam("id") final Long id
	) {
		final SimulationRun entity = resultsProxy.getSimulationResult(id);

		if (entity == null) {
			throw new WebApplicationException(Response.Status.NOT_FOUND);
		}
		return Response.ok(entity).build();
	}

	@DELETE
	@Path("/result/{id}")
	public Response deleteSimulationResult(@PathParam("id") final Long id) {
		if (!resultsProxy.deleteSimulationResult(id)) {
			throw new WebApplicationException(Response.Status.NOT_FOUND);
		}

		return Response.ok().build();
	}
}
