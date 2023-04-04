package software.uncharted.terarium.hmiserver.proxies.jsdelivr;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import javax.ws.rs.*;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@RegisterRestClient(configKey = "jsdelivr")
public interface JsDelivrProxy {
    @GET
	@Path("/gh/{repoOwnerAndName}@latest/{path}")
	String getGithubCode(
		@PathParam("repoOwnerAndName") String repoOwnerAndName,
		@PathParam("path") String path
	);
}