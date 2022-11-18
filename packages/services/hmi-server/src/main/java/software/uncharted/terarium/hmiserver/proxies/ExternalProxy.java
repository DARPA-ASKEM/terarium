package software.uncharted.terarium.hmiserver.proxies;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@RegisterRestClient(configKey = "data-service")
@Path("/external")
@Produces(MediaType.APPLICATION_JSON)
public interface ExternalProxy {

	@GET
	@Path("/software/{id}")
	Response getSoftware(
		@PathParam("id") String id
	);

	@DELETE
	@Path("/software/{id}")
	Response deleteSoftware(
		@PathParam("id") String id
	);

	@POST
	@Path("/software")
	@Consumes(MediaType.APPLICATION_JSON)
	Response createSoftware();

	@GET
	@Path("/publications/{id}")
	Response getPublication(
		@PathParam("id") String id
	);

	@DELETE
	@Path("/publications/{id}")
	Response deletePublication(
		@PathParam("id") String id
	);

	@POST
	@Path("/publications")
	@Consumes(MediaType.APPLICATION_JSON)
	Response createPublication();
}
