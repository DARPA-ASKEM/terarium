package software.uncharted.terarium.mockdataservice.resources;

import software.uncharted.terarium.mockdataservice.models.SimulationRun;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Path("/results")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SimulationResultResource {
	private final Map<Long, SimulationRun> results;

	public SimulationResultResource() {
		this.results = new HashMap<>();
		this.results.put(1L, new SimulationRun(
			1L,
			1L,
			true,
			"{data: \"This was a successful simulation run\"}")
		);
		this.results.put(2L, new SimulationRun(
			2L,
			2L,
			false,
			"{data: \"This was a failed simulation run\"}")
		);
	}

	@GET
	public Collection<SimulationRun> getSimulationResults() {
		return this.results.values();
	}

	@GET
	@Path("/{id}")
	public SimulationRun getSimulationResult(
		@PathParam("id") final Long id
	) {
		return this.results.getOrDefault(id, null);
	}

	@DELETE
	@Path("/{id}")
	@Produces(MediaType.TEXT_PLAIN)
	public Boolean deleteSimulationResult(@PathParam("id") final Long id) {
		if (this.results.containsKey(id)) {
			this.results.remove(id);
			return true;
		}
		return false;
	}
}
