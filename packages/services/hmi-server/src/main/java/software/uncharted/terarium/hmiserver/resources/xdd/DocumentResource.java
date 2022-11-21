package software.uncharted.terarium.hmiserver.resources.xdd;

import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import software.uncharted.terarium.hmiserver.proxies.xdd.DocumentProxy;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/api/xdd/documents")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "XDD Documents REST Endpoint")
public class DocumentResource {

	@RestClient
	DocumentProxy proxy;

	// NOTE: the query parameters match the proxy version and the type XDDSearchPayload
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Tag(name = "Get all xdd documents via proxy")
	public Response getDocuments(
		@QueryParam("doi") String doi,
		@QueryParam("title") String title,
		@QueryParam("term") String term
	) {
		// only go ahead with the query if at least one param is present
		if (doi != null || title != null || term != null) {
			// for consistency, if doi/title are valid, then make sure other params are null
			if (doi != null) {
				title = null;
				term = null;
			}
			if (title != null) {
				doi = null;
				term = null;
			}
			return proxy.getDocuments(doi, title, term);
		}
		return Response.noContent().build();
	}

}
