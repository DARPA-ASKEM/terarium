package software.uncharted.terarium.hmiserver.proxies.xdd;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@RegisterRestClient(configKey = "xdd-extraction-service")
@Produces(MediaType.APPLICATION_JSON)
public interface ExtractionProxy {
	@GET
	@Path("object")
	Object getExtractions(
		@QueryParam("doi") String doi
	);
}
