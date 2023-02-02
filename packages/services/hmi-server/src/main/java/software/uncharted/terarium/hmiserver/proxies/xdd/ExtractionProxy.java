package software.uncharted.terarium.hmiserver.proxies.xdd;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@RegisterRestClient(configKey = "extraction-service")
@Produces(MediaType.APPLICATION_JSON)
@Path("/api/xdd/extractions")
public interface ExtractionProxy {
	@GET
	Response getExtractions(
		@QueryParam("term") String term,
		@QueryParam("page") Integer page,
		@QueryParam("ASKEM_CLASS") String askemClass
	);

	@GET
	@Path("askem_autocomplete/{term}")
	Response getAutocomplete(
		@PathParam("term") final String term
	);
}
