package software.uncharted.terarium.mockdataservice.resources;

import software.uncharted.terarium.mockdataservice.models.SimulationPlan;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;

@Path("/plan")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SimulationPlanResource {

	@GET
	public List<SimulationPlan> getSimulationPlans() {
		final SimulationPlan spA = new SimulationPlan(1L, 1L, "Simulation Plan A", "Test Simulation Plan A", "Simulator A", "Simulation Plan Query A", "{data: \"simulation A data\"}");
		final SimulationPlan spB = new SimulationPlan(2L, 2L, "Simulation Plan B", "Test Simulation Plan B", "Simulator B", "Simulation Plan Query B", "{data: \"simulation B data\"}");

		final List<SimulationPlan> response = new ArrayList<>(2);
		response.add(spA);
		response.add(spB);

		return response;
	}

	@GET
	@Path("/{id}")
	public SimulationPlan getSimulationPlan(
		@QueryParam("id") final Long id
	) {
		final SimulationPlan spA = new SimulationPlan(1L, 1L, "Simulation Plan A", "Test Simulation Plan A", "Simulator A", "Simulation Plan Query A", "{data: \"simulation A data\"}");
		final SimulationPlan spB = new SimulationPlan(2L, 2L, "Simulation Plan B", "Test Simulation Plan B", "Simulator B", "Simulation Plan Query B", "{data: \"simulation B data\"}");

		if (id.equals(spA.getId())) {
			return spA;
		} else if (id.equals(spB.getId())) {
			return spB;
		}

		return null;
	}

	@POST
	public SimulationPlan createSimulationPlan(final SimulationPlan newPlan) {
		return newPlan;
	}

	@PUT
	@Path("/{id}")
	public SimulationPlan updateSimulationPlan(final Long id, final SimulationPlan updatedPlan) {
		return updatedPlan;
	}

	@DELETE
	@Path("/{id}")
	@Produces(MediaType.TEXT_PLAIN)
	public Boolean deleteSimulationPlan(final Long id) {
		return true;
	}
}
