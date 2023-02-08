package software.uncharted.terarium.documentserver.resources.xdd;

import io.quarkus.cache.CacheInvalidate;
import io.quarkus.cache.CacheResult;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import software.uncharted.terarium.documentserver.caching.CacheClearService;
import software.uncharted.terarium.documentserver.responses.xdd.XDDDictionariesResponseOK;
import software.uncharted.terarium.documentserver.responses.xdd.XDDResponse;
import software.uncharted.terarium.documentserver.proxies.xdd.DocumentProxy;


import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;


@Tag(name = "XDD Dictionaries REST Endpoint")
@Path("/api")
@Slf4j
public class DictionariesResource {
	// Also defined in application.properties
	private static final String CACHE_NAME = "xDD-Dictionaries";

	@RestClient
	DocumentProxy proxy;

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Tag(name = "Get available XDD dictionaries")
	@Path("/dictionaries")
	@CacheResult(cacheName = CacheClearService.CacheName.Constants.XDD_DICTIONARIES_NAME)
	public XDDResponse<XDDDictionariesResponseOK> getAvailableDictionaries(@QueryParam("all") @DefaultValue("") String all) {
		return proxy.getAvailableDictionaries(all);

	}
	
}
