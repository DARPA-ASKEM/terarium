package software.uncharted.terarium.hmiserver.proxies.modelservice;

import software.uncharted.terarium.hmiserver.models.modelservice.StratifyRequest;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import software.uncharted.terarium.hmiserver.annotations.LogRestClientTime;
import software.uncharted.terarium.hmiserver.models.modelservice.PetriNet;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@RegisterRestClient(configKey = "model-service")
@Produces(MediaType.APPLICATION_JSON)
public interface ModelServiceProxy {
	@POST
	@Path("/api/petri-to-latex")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@LogRestClientTime
	Response petrinetToLatex(
			PetriNet content
	);

	@POST
	@Path("/api/stratify")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@LogRestClientTime
	Response stratify(
			StratifyRequest req
	);

}
