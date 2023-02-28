package software.uncharted.terarium.hmiserver.resources.dataservice;

import io.quarkus.security.Authenticated;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import software.uncharted.terarium.hmiserver.models.dataservice.Publication;
import software.uncharted.terarium.hmiserver.models.dataservice.Software;
import software.uncharted.terarium.hmiserver.proxies.dataservice.ExternalProxy;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/api/external")
@Authenticated
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "External REST Endpoints")
public class ExternalResource {

	@Inject
	@RestClient
	ExternalProxy proxy;

	@GET
	@Path("/software/{id}")
	public Response getSoftware(
		@PathParam("id") final String id
	) {
		return proxy.getSoftware(id);
	}

	@DELETE
	@Path("/software/{id}")
	public Response deleteSoftware(
		@PathParam("id") final String id
	) {
		return proxy.deleteSoftware(id);
	}

	@POST
	@Path("/software")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createSoftware(
		final Software software
	) {
		return proxy.createSoftware(software);
	}

	@GET
	@Path("/publications/{id}")
	public Response getPublication(
		@PathParam("id") final String id
	) {
		return proxy.getPublication(id);
	}

	@DELETE
	@Path("/publications/{id}")
	public Response deletePublication(
		@PathParam("id") final String id
	) {
		return proxy.deletePublication(id);
	}

	@POST
	@Path("/publications")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createPublication(
		final Publication publication
	) {
		return proxy.createPublication(publication);
	}
}
