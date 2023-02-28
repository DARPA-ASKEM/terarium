package software.uncharted.terarium.hmiserver.proxies.dataservice;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import software.uncharted.terarium.hmiserver.models.dataservice.Publication;
import software.uncharted.terarium.hmiserver.models.dataservice.Software;

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
	Response createSoftware(
		Software software
	);

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
	Response createPublication(
		Publication publication
	);
}
