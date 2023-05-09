package software.uncharted.terarium.hmiserver.resources.dataservice;

import io.quarkus.security.Authenticated;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import software.uncharted.terarium.hmiserver.exceptions.HmiResponseExceptionMapper;
import software.uncharted.terarium.hmiserver.models.dataservice.DocumentAsset;
import software.uncharted.terarium.hmiserver.models.dataservice.Software;
import software.uncharted.terarium.hmiserver.proxies.dataservice.ExternalProxy;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/api/external")

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
	@APIResponses({
		@APIResponse(responseCode = "500", description = "An error occurred retrieving publicatione information"),
		@APIResponse(responseCode = "204", description = "No publication with this ID found, but request was valid")})
	public Response getPublication(
		@PathParam("id") final String id
	) {
		try {
			return proxy.getPublication(id);
		} catch (HmiResponseExceptionMapper.ResponseRuntimeException e) {
			if (e.getStatus() == 404) {
				return Response.noContent().build();
			} else {
				return Response
					.status(Response.Status.INTERNAL_SERVER_ERROR)
					.type(MediaType.APPLICATION_JSON)
					.build();
			}
		}
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
		final DocumentAsset publication
	) {
		return proxy.createPublication(publication);
	}
}
