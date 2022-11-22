package software.uncharted.terarium.hmiserver.resources.xdd;

import io.quarkus.security.Authenticated;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import software.uncharted.terarium.hmiserver.proxies.xdd.DocumentProxy;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/api/xdd/documents")
@Authenticated
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
		@QueryParam("term") String term,
		@QueryParam("dataset") String dataset,
		@QueryParam("include_score") String include_score,
		@QueryParam("full_results") String full_results,
		@QueryParam("max") String max,
		@QueryParam("per_page") String per_page,
		@QueryParam("dict") String dict,
		@QueryParam("facets") String facets,
		@QueryParam("min_published") String min_published,
		@QueryParam("max_published") String max_published,
		@QueryParam("pubname") String pubname,
		@QueryParam("publisher") String publisher
	) {
		// only go ahead with the query if at least one param is present
		if (doi != null || title != null || term != null) {
			// for consistency, if doi/title are valid, then make sure other params are null
			if (doi != null) {
				title = null;
				term = null;
				dataset = null;
				include_score = null;
				full_results = null;
				max = null;
				per_page = null;
				dict = null;
				min_published = null;
				max_published = null;
				pubname = null;
				publisher = null;
			}
			if (title != null) {
				doi = null;
				term = null;
				dataset = null;
				include_score = null;
				full_results = null;
				max = null;
				per_page = null;
				dict = null;
				min_published = null;
				max_published = null;
				pubname = null;
				publisher = null;
			}
			return proxy.getDocuments(
				doi, title, term, dataset, include_score, full_results, max, per_page, dict, facets,
				min_published, max_published, pubname, publisher);
		}
		return Response.noContent().build();
	}

}
