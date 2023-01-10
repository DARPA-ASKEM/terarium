package software.uncharted.terarium.documentserver.resources.xdd;

import io.quarkus.security.Authenticated;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import software.uncharted.terarium.documentserver.proxies.xdd.DocumentProxy;


import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


@Tag(name = "XDD Dictionaries REST Endpoint")
@Path("/api")
public class DictionariesResource {

	@RestClient
	DocumentProxy proxy;

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Tag(name = "Get available XDD dictionaries")
	@Path("/dictionaries")
	public Response getAvailableDictionaries(@QueryParam("all") @DefaultValue("") String all) {
		return proxy.getAvailableDictionaries(all);
	}
}
