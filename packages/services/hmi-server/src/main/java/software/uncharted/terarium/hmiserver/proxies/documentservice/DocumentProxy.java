package software.uncharted.terarium.hmiserver.proxies.documentservice;

import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import software.uncharted.terarium.hmiserver.exceptions.HmiResponseExceptionMapper;
import software.uncharted.terarium.hmiserver.resources.documentservice.responses.*;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@RegisterRestClient(configKey = "xdd-document-service")
@Produces(MediaType.APPLICATION_JSON)
@RegisterProvider(HmiResponseExceptionMapper.class)
public interface DocumentProxy {
	@GET
	@Path("/api/articles")
	XDDResponse<DocumentsResponseOK> getDocuments(
		@QueryParam("docid") String docid,
		@QueryParam("doi") String doi,
		@QueryParam("title") String title,
		@QueryParam("term") String term,
		@QueryParam("dataset") String dataset,
		@QueryParam("include_score") String include_score,
		@QueryParam("include_highlights") String include_highlights,
		@QueryParam("inclusive") String inclusive,
		@QueryParam("full_results") String full_results,
		@QueryParam("max") String max,
		@QueryParam("per_page") String per_page,
		@QueryParam("dict") String dict,
		@QueryParam("facets") String facets,
		@QueryParam("min_published") String min_published,
		@QueryParam("max_published") String max_published,
		@QueryParam("pubname") String pubname,
		@QueryParam("publisher") String publisher,
		@QueryParam("additional_fields") String additional_fields,
		@QueryParam("match") String match,
		@QueryParam("known_entities") String known_entities,
		@QueryParam("github_url") String github_url
	);

	@GET
	@Path("/sets")
	XDDSetsResponse getAvailableSets();

	@GET
	@Path("/api/dictionaries")
	XDDResponse<XDDDictionariesResponseOK> getAvailableDictionaries(@QueryParam("all") @DefaultValue("") String all);

	@GET
	@Path("/sets/{set}/doc2vec/api/similar")
	XDDRelatedDocumentsResponse getRelatedDocuments(
		@PathParam("set") String set,
		@QueryParam("docid") String docid
	);

	@GET
	@Path("/sets/{set}/word2vec/api/most_similar")
	XDDRelatedWordsResponse getRelatedWords(
		@PathParam("set") String set,
		@QueryParam("word") String word
	);

}
