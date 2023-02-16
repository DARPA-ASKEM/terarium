package software.uncharted.terarium.hmiserver.resources.documentservice;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import software.uncharted.terarium.hmiserver.resources.documentservice.responses.XDDRelatedDocumentsResponse;
import software.uncharted.terarium.hmiserver.proxies.documentservice.DocumentProxy;
import software.uncharted.terarium.hmiserver.resources.documentservice.responses.XDDRelatedWordsResponse;


import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Related documents / words REST Endpoint")
@Path("/api/document/related")
@Slf4j
public class RelatedResource {

	@RestClient
	DocumentProxy proxy;

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/document")
	@Tag(name = "Get related documents for a given XDD internal doc id")
	@APIResponses({
		@APIResponse(responseCode = "500", description = "An error occurred retrieving related documents"),
		@APIResponse(responseCode = "204", description = "Request received successfully, but there are no related documents")})
	public Response getRelatedDocuments(
		@QueryParam("set") String set,
		@QueryParam("docid") String docid) {
		try {
			XDDRelatedDocumentsResponse response = proxy.getRelatedDocuments(set, docid);
			if (response.getData() == null || response.getData().isEmpty())
				return Response.noContent().build();

			return Response
				.status(Response.Status.OK)
				.entity(response)
				.type(MediaType.APPLICATION_JSON)
				.build();

		} catch (RuntimeException e) {
			log.error("There was an error finding related documents", e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/word")
	@Tag(name = "Get most similar words for a given string")
	@APIResponses({
		@APIResponse(responseCode = "500", description = "An error occurred retrieving related words"),
		@APIResponse(responseCode = "204", description = "Request received successfully, but there are no related words")})
	public Response getRelatedWords(
		@QueryParam("set") String set,
		@QueryParam("word") String word) {
		try {
			XDDRelatedWordsResponse response = proxy.getRelatedWords(set, word);
			if (response.getData() == null || response.getData().isEmpty()) {
				return Response.noContent().build();
			}

			return Response
				.status(Response.Status.OK)
				.entity(response)
				.type(MediaType.APPLICATION_JSON)
				.build();

		} catch (RuntimeException e) {
			log.error("An error occurred finding related words", e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}
}
