package software.uncharted.terarium.hmiserver.resources.documentservice;


import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import io.quarkus.cache.CacheResult;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import software.uncharted.terarium.hmiserver.caching.CacheClearService;
import software.uncharted.terarium.hmiserver.resources.documentservice.responses.XDDSetsResponse;
import software.uncharted.terarium.hmiserver.proxies.documentservice.DocumentProxy;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/document/sets")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Sets REST Endpoint")
@Slf4j
public class SetResource {

	@RestClient
	DocumentProxy proxy;

	@GET
	@Produces(MediaType.APPLICATION_JSON)

	@Tag(name = "Get available sets or collections")
	@CacheResult(cacheName = CacheClearService.CacheName.Constants.XDD_SETS_NAME)
	@APIResponses({
		@APIResponse(responseCode = "500", description = "An error occurred retrieving sets"),
		@APIResponse(responseCode = "204", description = "Request received successfully, but there are no sets")})
	public Response getAvailableSets() {

		try {
			XDDSetsResponse response = proxy.getAvailableSets();

			if (response.getAvailableSets() == null || response.getAvailableSets().isEmpty())
				return Response.noContent().build();

			return Response
				.status(Response.Status.OK)
				.entity(response)
				.type(MediaType.APPLICATION_JSON)
				.build();

		} catch (RuntimeException e) {
			log.error("There was an error finding available sets", e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

}
