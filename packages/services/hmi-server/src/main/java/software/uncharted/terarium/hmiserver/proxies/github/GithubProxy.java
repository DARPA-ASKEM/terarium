package software.uncharted.terarium.hmiserver.proxies.github;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import software.uncharted.terarium.hmiserver.models.code.GithubFile;

import javax.ws.rs.*;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import java.util.List;

@RegisterRestClient(configKey = "github")
public interface GithubProxy {
    @GET
	@Path("/repos/{repoOwnerAndName}/contents/{path}")
		List<GithubFile> getGithubRepositoryContent(
		@PathParam("repoOwnerAndName") String repoOwnerAndName,
		@PathParam("path") String path
	);
}
