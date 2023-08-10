package software.uncharted.terarium.hmiserver.proxies.jsdelivr;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import software.uncharted.terarium.hmiserver.annotations.LogRestClientTime;

import jakarta.ws.rs.*;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

@RegisterRestClient(configKey = "jsdelivr")
public interface JsDelivrProxy {
	@GET
	@Path("/gh/{repoOwnerAndName}@latest/{path}")
	@LogRestClientTime
	String getGithubCode(
		@PathParam("repoOwnerAndName") String repoOwnerAndName,
		@PathParam("path") String path
	);
}
