package software.uncharted.terarium.documentserver.resources.xdd;

import io.quarkus.cache.CacheInvalidate;
import io.quarkus.cache.CacheResult;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import software.uncharted.terarium.documentserver.responses.xdd.XDDSetsResponse;
import software.uncharted.terarium.documentserver.proxies.xdd.DocumentProxy;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("/sets")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "XDD Sets REST Endpoint")
@Slf4j
public class SetResource {

	private static final String CACHE_NAME = "xDD-Sets";

	@RestClient
	DocumentProxy proxy;

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Tag(name = "Get available XDD sets or collections")
	@CacheResult(cacheName = CACHE_NAME)
	public XDDSetsResponse getAvailableSets() {

		try {
			return proxy.getAvailableSets();
		} catch (RuntimeException e) {
			log.error("Unable to get sets. Invalidating cache.", e);
			invalidateXDDCache();
		}
		return null;
	}

	@CacheInvalidate(cacheName = CACHE_NAME)
	public void invalidateXDDCache() {
	}
}
