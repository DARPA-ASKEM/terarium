package software.uncharted.terarium.hmiserver.proxies.mira;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;


import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@RegisterRestClient(configKey = "mira-service")
@Produces(MediaType.APPLICATION_JSON)
public interface MiraProxy {
	@GET
	@Path("/api/entity/{curie}")
	Response getEntity(
		@PathParam("curie") final String curie
	);

	@GET
	@Path("/api/entities/{curies}")
	Response getEntities(
		@PathParam("curies") final String curies
	);
}
