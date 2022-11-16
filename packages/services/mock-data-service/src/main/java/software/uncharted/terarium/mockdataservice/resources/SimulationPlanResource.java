package software.uncharted.terarium.mockdataservice.resources;

import software.uncharted.terarium.mockdataservice.models.SimulationPlan;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Path("/plans")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SimulationPlanResource {

	private final Map<Long, SimulationPlan> plans;

	public SimulationPlanResource() {
		this.plans = new HashMap<>();
		this.plans.put(1L, new SimulationPlan(
			1L,
			1L,
			"Simulation Plan A",
			"Test Simulation Plan A",
			"Simulator A",
			"Simulation Plan Query A",
			"{data: \"simulation A data\"}")
		);
		this.plans.put(2L, new SimulationPlan(
			2L,
			2L,
			"Simulation Plan B",
			"Test Simulation Plan B",
			"Simulator B",
			"Simulation Plan Query B",
			"{data: \"simulation B data\"}")
		);
	}

	@GET
	public Collection<SimulationPlan> getSimulationPlans() {
		return this.plans.values();
	}

	@GET
	@Path("/{id}")
	public SimulationPlan getSimulationPlan(
		@PathParam("id") final Long id
	) {
		return this.plans.getOrDefault(id, null);
	}

	@POST
	public SimulationPlan createSimulationPlan(
		final SimulationPlan newPlan
	) {
		if (this.plans.containsKey(newPlan.getId())) return null;
		this.plans.put(newPlan.getId(), newPlan);
		return newPlan;
	}

	@PUT
	@Path("/{id}")
	public SimulationPlan updateSimulationPlan(@PathParam("id") final Long id, final SimulationPlan updatedPlan) {
		if (this.plans.containsKey(updatedPlan.getId())) {
			this.plans.replace(updatedPlan.getId(), updatedPlan);
			return updatedPlan;
		}
		return null;
	}

	@DELETE
	@Path("/{id}")
	@Produces(MediaType.TEXT_PLAIN)
	public Boolean deleteSimulationPlan(@PathParam("id") final Long id) {
		if (this.plans.containsKey(id)) {
			this.plans.remove(id);
			return true;
		}
		return false;
	}
}
