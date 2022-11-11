package software.uncharted.terarium.mockdataservice.resources;

import software.uncharted.terarium.mockdataservice.models.SimulationRun;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;

@Path("/result")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SimulationResultResource {

	@GET
	public List<SimulationRun> getSimulationResults() {
		final SimulationRun srA = new SimulationRun(1L, 1L, true, "{data: \"This was a successful simulation run\"}");
		final SimulationRun srB = new SimulationRun(2L, 2L, false, "{data: \"This was a failed simulation run\"}");

		final List<SimulationRun> response = new ArrayList<>(2);
		response.add(srA);
		response.add(srB);

		return response;
	}

	@GET
	@Path("/{id}")
	public SimulationRun getSimulationResult(
		@QueryParam("id") final Long id
	) {
		final SimulationRun srA = new SimulationRun(1L, 1L, true, "{data: \"This was a successful simulation run\"}");
		final SimulationRun srB = new SimulationRun(2L, 2L, false, "{data: \"This was a failed simulation run\"}");

		if (id.equals(srA.getId())) {
			return srA;
		} else if (id.equals(srB.getId())) {
			return srB;
		}

		return null;
	}

	@DELETE
	@Path("/{id}")
	@Produces(MediaType.TEXT_PLAIN)
	public Boolean deleteSimulationResult(final Long id) {
		return true;
	}
}
