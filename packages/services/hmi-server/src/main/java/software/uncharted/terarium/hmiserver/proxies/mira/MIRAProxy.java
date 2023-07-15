package software.uncharted.terarium.hmiserver.proxies.mira;

import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import software.uncharted.terarium.hmiserver.annotations.LogRestClientTime;
import software.uncharted.terarium.hmiserver.exceptions.HmiResponseExceptionMapper;
import software.uncharted.terarium.hmiserver.models.mira.DKG;


import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@RegisterProvider(HmiResponseExceptionMapper.class)
@RegisterRestClient(configKey = "mira-api")
@Produces(MediaType.APPLICATION_JSON)
@Path("/api")
public interface MIRAProxy {
	@GET
	@Path("/entity/{curie}")
	@LogRestClientTime
	DKG getEntity(
		@PathParam("curie") final String curie
	);

	@GET
	@Path("/entities/{curies}")
	@LogRestClientTime
	List<DKG> getEntities(
		@PathParam("curies") final String curies
	);

	@POST
	@Path("/api/reconstruct_ode_semantics")
	@LogRestClientTime
	Object recconstructAMR(
			final Object amr
	);

}
