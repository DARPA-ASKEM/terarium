package software.uncharted.terarium.hmiserver.resources;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import software.uncharted.terarium.hmiserver.models.XDD.Document;
import software.uncharted.terarium.hmiserver.models.XDD.Extraction;
import software.uncharted.terarium.hmiserver.models.XDD.XDDArticlesResponseOK;
import software.uncharted.terarium.hmiserver.models.XDD.XDDResponse;
import software.uncharted.terarium.hmiserver.proxies.DocumentProxy;
import software.uncharted.terarium.hmiserver.services.DocumentService;

import io.quarkus.security.identity.SecurityIdentity;

@Path("/api/document")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "XDD Documents REST Endpoint")
public class DocumentsResource {

    @Inject
    SecurityIdentity identity;

		@Inject
		DocumentService documentService;

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
			List<Document> documents = new ArrayList<>();
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
				try {
					final XDDResponse<XDDArticlesResponseOK> res = proxy.getDocuments(doi, title, term);
					for (Document doc : res.success.data) {
						documents.add(doc);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return Response.ok(documents).build();
		}

		@POST
		@Consumes(MediaType.TEXT_PLAIN)
		@Produces(MediaType.APPLICATION_JSON)
		@Tag(name = "Search XDD for documents based on the provided parameters in the payload")
		public Response searchDocuments(final String jsonPayload) {
			final List<Document> documents = documentService.getDocuments(jsonPayload);
			return Response.ok(documents).build();
		}

		@Path("/extractions")
		@POST
		@Consumes(MediaType.TEXT_PLAIN)
		@Produces(MediaType.APPLICATION_JSON)
		@Tag(name = "Search XDD for extractions related to the document identified in the payload")
		public Response searchExtractions(final String jsonPayload) {
			final List<Extraction> documents = documentService.getExtractions(jsonPayload);
			return Response.ok(documents).build();
		}
}
