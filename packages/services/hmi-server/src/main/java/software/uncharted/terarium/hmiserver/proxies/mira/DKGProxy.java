package software.uncharted.terarium.hmiserver.proxies.mira;

import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import software.uncharted.terarium.hmiserver.exceptions.HmiResponseExceptionMapper;
import software.uncharted.terarium.hmiserver.models.mira.DKG;


import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@RegisterProvider(HmiResponseExceptionMapper.class)
@RegisterRestClient(configKey = "mira-dkg")
@Produces(MediaType.APPLICATION_JSON)
@Path("/api")
public interface DKGProxy {
	@GET
	@Path("/entity/{curie}")
	DKG getEntity(
		@PathParam("curie") final String curie
	);

	@GET
	@Path("/entities/{curies}")
	List<DKG> getEntities(
		@PathParam("curies") final String curies
	);
}
