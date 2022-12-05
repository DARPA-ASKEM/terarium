package software.uncharted.terarium.hmiserver.resources.xdd;

import io.quarkus.security.Authenticated;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import software.uncharted.terarium.hmiserver.proxies.xdd.DocumentProxy;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/api/xdd/related")
@Authenticated
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "XDD Related documents / words REST Endpoint")
public class RelatedResource {

	@RestClient
	DocumentProxy proxy;

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/document")
	@Tag(name = "Get related documents for a given XDD internal doc id")
	public Response getRelatedDocuments(
		@QueryParam("set") String set,
		@QueryParam("docid") String docid) {
		return proxy.getRelatedDocuments(set, docid);
	}
}
