package software.uncharted.terarium.hmiserver.proxies.simulationservice;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import com.fasterxml.jackson.databind.JsonNode;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import software.uncharted.terarium.hmiserver.models.simulationservice.SimulationRequest;
import software.uncharted.terarium.hmiserver.models.simulationservice.JobResponse;


@RegisterRestClient(configKey = "ciemss-service")
@Produces(MediaType.APPLICATION_JSON)
public interface SimulationCiemssServiceProxy {
	@POST
	@Path("/simulate")
	@Consumes(MediaType.APPLICATION_JSON)
	JobResponse makeForecastRun(
		JsonNode request
	);

	@POST
	@Path("/calibrate")
	@Consumes(MediaType.APPLICATION_JSON)
	JobResponse makeCalibrateJob(
		JsonNode request
	);

	@POST
	@Path("/ensemble-simulate")
	@Consumes(MediaType.APPLICATION_JSON)
	JobResponse makeEnsembleSimulateJob(
		JsonNode request
	);

	@GET
	@Path("/status/{runId}")
	@Consumes(MediaType.APPLICATION_JSON)
	Response getRunStatus(
		@PathParam("runId") String runId
	);
}
