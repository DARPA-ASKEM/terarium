package software.uncharted.terarium.hmiserver.resources.documentservice;


import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import software.uncharted.terarium.hmiserver.resources.documentservice.responses.DocumentsResponseOK;
import software.uncharted.terarium.hmiserver.resources.documentservice.responses.XDDResponse;
import software.uncharted.terarium.hmiserver.proxies.documentservice.DocumentProxy;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Optional;


@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Documents REST Endpoint")
@Path("/api")
@Slf4j
public class DocumentResource {

	@RestClient
	DocumentProxy proxy;

	@ConfigProperty(name = "xdd.api_es_key")
	Optional<String> apiKey;

	// NOTE: the query parameters match the proxy version and the type XDDSearchPayload
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Tag(name = "Get all xdd documents via proxy")
	@Path("/documents")
	@APIResponses({
		@APIResponse(responseCode = "500", description = "An error occurred retrieving documents"),
		@APIResponse(responseCode = "204", description = "Request received successfully, but there are documents"),
		@APIResponse(responseCode = "400", description = "Query must contain one of docid, doi or term")})
	public Response getDocuments(
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
		@QueryParam("github_url") String github_url,
		@QueryParam("similar_docs") String similar_docs
	) {

		// only go ahead with the query if at least one param is present
		if (docid != null || doi != null || term != null || github_url != null) {
			// for a more direct search, if doi is valid, then make sure other params (except similar_docs) are null
			if (docid != null || doi != null) {
				title = null;
				term = null;
				dataset = null;
				include_score = null;
				include_highlights = null;
				inclusive = null;
				full_results = null;
				max = null;
				per_page = null;
				dict = null;
				min_published = null;
				max_published = null;
				pubname = null;
				publisher = null;
				additional_fields = null;
				match = null;

				// similar_docs can only be queried in conjuntion with 'docid' or 'doi'
				if (similar_docs != null) {
					github_url = null;
				}
			}

			// // if similar_docs, make sure that only docid is present
			// if (similar_docs != null && (docid != null || doi != null)) {
			// 	if (term != null || github_url != null) {
			// 		log.error("You cannot query with 'term' or 'github_url' when querying 'similar_docs'");
			// 		return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
			// 	}
			// }

			try {


				String apiKey = "";
				if (this.apiKey.isPresent())
					apiKey = this.apiKey.get();
				else {
					log.error("XDD API key missing. Extractions will not work");
					return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
				}

				XDDResponse<DocumentsResponseOK> doc = proxy.getDocuments(apiKey,
					docid, doi, title, term, dataset, include_score, include_highlights, inclusive, full_results, max, per_page, dict, facets,
					min_published, max_published, pubname, publisher, additional_fields, match, known_entities, github_url, similar_docs);




				if (doc.getErrorMessage() != null) {
					return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
						.entity(doc.getErrorMessage())
						.build();
				}

				if (doc.getSuccess() == null || doc.getSuccess().getData().isEmpty()) {
					return Response.noContent().build();
				}

				return Response.status(Response.Status.OK).entity(doc).build();

			} catch (RuntimeException e) {
				log.error("Unable to find documents, an error occurred", e);
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
			}


		}
		return Response.status(Response.Status.BAD_REQUEST).build();
	}

}
