package software.uncharted.terarium.hmiserver.proxies.simulationservice;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import com.fasterxml.jackson.databind.JsonNode;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import software.uncharted.terarium.hmiserver.annotations.LogRestClientTime;
import software.uncharted.terarium.hmiserver.models.simulationservice.SimulationRequest;
import software.uncharted.terarium.hmiserver.models.simulationservice.CalibrationRequest;
import software.uncharted.terarium.hmiserver.models.simulationservice.JobResponse;


@RegisterRestClient(configKey = "simulation-service")
@Produces(MediaType.APPLICATION_JSON)
public interface SimulationServiceProxy {
	@POST
	@Path("/simulate")
	@Consumes(MediaType.APPLICATION_JSON)
	@LogRestClientTime
	JobResponse makeForecastRun(
		JsonNode request
	);

	@POST
	@Path("/calibrate")
	@Consumes(MediaType.APPLICATION_JSON)
	@LogRestClientTime
	JobResponse makeCalibrateJob(
		JsonNode request
	);

	@GET
	@Path("/runs/{runId}/status")
	@Consumes(MediaType.APPLICATION_JSON)
	@LogRestClientTime
	Response getRunStatus(
		@PathParam("runId") String runId
	);

	@GET
	@Path("/runs/{runId}/result")
	@Consumes(MediaType.APPLICATION_JSON)
	@LogRestClientTime
	Response getRunResult(
		@PathParam("runId") String runId
	);
}
