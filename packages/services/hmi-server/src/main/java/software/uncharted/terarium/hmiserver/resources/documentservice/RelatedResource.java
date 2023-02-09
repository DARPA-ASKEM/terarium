package software.uncharted.terarium.hmiserver.resources.documentservice;

import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import software.uncharted.terarium.hmiserver.resources.documentservice.responses.XDDRelatedDocumentsResponse;
import software.uncharted.terarium.hmiserver.proxies.documentservice.DocumentProxy;
import software.uncharted.terarium.hmiserver.resources.documentservice.responses.XDDRelatedWordsResponse;


import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Related documents / words REST Endpoint")
@Path("/api/document/related")
public class RelatedResource {

	@RestClient
	DocumentProxy proxy;

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/document")
	@Tag(name = "Get related documents for a given XDD internal doc id")
	public XDDRelatedDocumentsResponse getRelatedDocuments(
		@QueryParam("set") String set,
		@QueryParam("docid") String docid) {
		return proxy.getRelatedDocuments(set, docid);
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/word")
	@Tag(name = "Get most similar words for a given string")
	public XDDRelatedWordsResponse getRelatedWords(
		@QueryParam("set") String set,
		@QueryParam("word") String word) {
		return proxy.getRelatedWords(set, word);
	}
}
