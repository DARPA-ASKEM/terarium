package software.uncharted.terarium.documentserver.resources.xdd;

import io.quarkus.security.Authenticated;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import software.uncharted.terarium.documentserver.proxies.xdd.ExtractionProxy;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


@Authenticated
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "XDD Extraction REST Endpoint")
@Path("/object")
public class ExtractionResource {

	@RestClient
	ExtractionProxy proxy;

	@GET
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)
	@Tag(name = "Search XDD for extractions related to the document identified in the payload")
	public Response searchExtractions(@QueryParam("doi") final String doi, @QueryParam("query_all") final String query_all) {
		return proxy.getExtractions(doi, query_all);
	}
}
