package software.uncharted.terarium.hmiserver.proxies.mira;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@RegisterRestClient(configKey = "mira-metaregistry")
@Produces(MediaType.APPLICATION_JSON)
public interface MetaRegistryProxy {
	@GET
	@Path("/{curie}")
	Response getEntity(
		@PathParam("curie") final String curie
	);
}
