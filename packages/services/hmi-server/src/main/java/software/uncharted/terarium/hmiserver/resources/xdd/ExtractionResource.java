package software.uncharted.terarium.hmiserver.resources.xdd;

import io.quarkus.security.Authenticated;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.annotations.Query;
import software.uncharted.terarium.hmiserver.proxies.xdd.ExtractionProxy;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/api/xdd/extractions")
@Authenticated
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "XDD Extraction REST Endpoint")
public class ExtractionResource {

	@RestClient
	ExtractionProxy proxy;

	@GET
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)
	@Tag(name = "Search XDD for extractions either using keyword search or for a given document identifier")
	public Response searchExtractions(@QueryParam("term") final String term, @QueryParam("page") final Integer page, @QueryParam("ASKEM_CLASS") String askemClass) {
		return proxy.getExtractions(term, page, askemClass);
	}

	@GET
	@Path("/askem_autocomplete/{term}")
	@Produces(MediaType.APPLICATION_JSON)
	@Tag(name = "Search XDD for extractions related to the document identified in the payload")
	public Response searchExtractions(@PathParam("term") String term) {
		return proxy.getAutocomplete(term);
	}
}
