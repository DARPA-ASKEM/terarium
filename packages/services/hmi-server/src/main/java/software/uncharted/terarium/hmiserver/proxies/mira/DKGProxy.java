package software.uncharted.terarium.hmiserver.proxies.mira;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import software.uncharted.terarium.hmiserver.models.mira.DKG;


import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@RegisterRestClient(configKey = "mira-dkg")
@Produces(MediaType.APPLICATION_JSON)
public interface DKGProxy {
	@GET
	@Path("/api/entity/{curie}")
	DKG getEntity(
		@PathParam("curie") final String curie
	);

	@GET
	@Path("/api/entities/{curies}")
	List<DKG> getEntities(
		@PathParam("curies") final String curies
	);
}
