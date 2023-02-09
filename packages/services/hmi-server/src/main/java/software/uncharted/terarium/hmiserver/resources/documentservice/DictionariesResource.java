package software.uncharted.terarium.hmiserver.resources.documentservice;

import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import software.uncharted.terarium.hmiserver.resources.documentservice.responses.XDDDictionariesResponseOK;
import software.uncharted.terarium.hmiserver.resources.documentservice.responses.XDDResponse;
import software.uncharted.terarium.hmiserver.proxies.documentservice.DocumentProxy;


import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;


@Tag(name = "Dictionaries REST Endpoint")
@Path("/api")
public class DictionariesResource {

	@RestClient
	DocumentProxy proxy;

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Tag(name = "Get available dictionaries")
	@Path("/dictionaries")
	public XDDResponse<XDDDictionariesResponseOK> getAvailableDictionaries(@QueryParam("all") @DefaultValue("") String all) {
		return proxy.getAvailableDictionaries(all);
	}
}
