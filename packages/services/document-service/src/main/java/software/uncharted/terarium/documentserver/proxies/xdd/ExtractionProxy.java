package software.uncharted.terarium.documentserver.proxies.xdd;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import software.uncharted.terarium.documentserver.models.xdd.XDDExtractionsResponseOK;
import software.uncharted.terarium.documentserver.models.xdd.XDDResponse;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

@RegisterRestClient(configKey = "xdd-extraction-service")
@Produces(MediaType.APPLICATION_JSON)
public interface ExtractionProxy {
	@GET
	@Path("object")
	XDDResponse<XDDExtractionsResponseOK> getExtractions(
		@QueryParam("doi") String doi,
		@QueryParam("query_all") String query_all
	);
}
