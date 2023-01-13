package software.uncharted.terarium.documentserver.resources.xdd;

import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import software.uncharted.terarium.documentserver.models.xdd.XDDRelatedDocumentsResponse;
import software.uncharted.terarium.documentserver.proxies.xdd.DocumentProxy;


import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "XDD Related documents / words REST Endpoint")
@Path("/sets")
public class RelatedResource {

	@RestClient
	DocumentProxy proxy;

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{set}/doc2vec/api/similar")
	@Tag(name = "Get related documents for a given XDD internal doc id")
	public XDDRelatedDocumentsResponse getRelatedDocuments(
		@PathParam("set") String set,
		@QueryParam("docid") String docid) {

		return proxy.getRelatedDocuments(set, docid);
	}
}
