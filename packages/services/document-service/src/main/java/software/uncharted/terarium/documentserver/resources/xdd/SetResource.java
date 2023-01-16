package software.uncharted.terarium.documentserver.resources.xdd;

import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import software.uncharted.terarium.documentserver.responses.xdd.XDDSetsResponse;
import software.uncharted.terarium.documentserver.proxies.xdd.DocumentProxy;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/sets")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "XDD Sets REST Endpoint")
public class SetResource {

	@RestClient
	DocumentProxy proxy;

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Tag(name = "Get available XDD sets or collections")
	public XDDSetsResponse getAvailableSets() {
		return proxy.getAvailableSets();
	}
}
