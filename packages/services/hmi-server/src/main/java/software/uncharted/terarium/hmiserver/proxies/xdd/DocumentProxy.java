package software.uncharted.terarium.hmiserver.proxies.xdd;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import software.uncharted.terarium.hmiserver.models.xdd.XDDArticlesResponseOK;
import software.uncharted.terarium.hmiserver.models.xdd.XDDResponse;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@RegisterRestClient(configKey = "xdd-document-service")
@Produces(MediaType.APPLICATION_JSON)
public interface DocumentProxy {
	@GET
	@Path("articles")
	XDDResponse<XDDArticlesResponseOK> getDocuments(
		@QueryParam("doi") String doi,
		@QueryParam("title") String title,
		@QueryParam("term") String term
	);
}
