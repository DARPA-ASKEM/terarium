package software.uncharted.terarium.hmiserver.resources.documentservice;


import io.quarkus.security.Authenticated;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import io.quarkus.cache.CacheResult;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import software.uncharted.terarium.hmiserver.caching.CacheClearService;
import software.uncharted.terarium.hmiserver.resources.documentservice.responses.XDDDictionariesResponseOK;
import software.uncharted.terarium.hmiserver.resources.documentservice.responses.XDDResponse;
import software.uncharted.terarium.hmiserver.proxies.documentservice.DocumentProxy;


import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


@Tag(name = "Dictionaries REST Endpoint")
@Path("/api")
@Slf4j

public class DictionariesResource {

	@RestClient
	DocumentProxy proxy;

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Tag(name = "Get available dictionaries")
	@Path("/dictionaries")
	@CacheResult(cacheName = CacheClearService.CacheName.Constants.XDD_DICTIONARIES_NAME)
	@APIResponses({
		@APIResponse(responseCode = "500", description = "An error getting available dictionaries"),
		@APIResponse(responseCode = "204", description = "Request received successfully, but there are no dictionaries")})
	public Response getAvailableDictionaries(@QueryParam("all") @DefaultValue("") String all) {
		try {
			XDDResponse<XDDDictionariesResponseOK> response = proxy.getAvailableDictionaries(all);

			if (response.getErrorMessage() != null) {
				Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity(response.getErrorMessage())
					.build();
			}


			if (response.getSuccess() == null || response.getSuccess().getData().isEmpty())
				return Response.noContent().build();

			return Response.status(Response.Status.OK)
				.entity(response)
				.type(MediaType.APPLICATION_JSON)
				.build();

		} catch (RuntimeException e) {
			log.error("Unable to get available dictionaries", e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

}
