package software.uncharted.terarium.hmiserver.resources;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import software.uncharted.terarium.hmiserver.models.XDD.Document;
import software.uncharted.terarium.hmiserver.models.XDD.Extraction;
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
