package software.uncharted.terarium.hmiserver.proxies.github;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import software.uncharted.terarium.hmiserver.annotations.LogRestClientTime;
import software.uncharted.terarium.hmiserver.models.code.GithubFile;

import jakarta.ws.rs.*;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import java.util.List;

@RegisterRestClient(configKey = "github")
public interface GithubProxy {
	@GET
	@Path("/repos/{repoOwnerAndName}/contents/{path}")
	@LogRestClientTime
	List<GithubFile> getGithubRepositoryContent(
		@PathParam("repoOwnerAndName") String repoOwnerAndName,
		@PathParam("path") String path
	);
}
