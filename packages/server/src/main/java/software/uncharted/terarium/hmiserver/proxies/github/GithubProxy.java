package software.uncharted.terarium.hmiserver.proxies.github;

import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import software.uncharted.terarium.hmiserver.models.code.GithubFile;

@FeignClient(name = "github", url = "${github.url}")
public interface GithubProxy {
	@GetMapping("/repos/{repoOwnerAndName}/contents/{path}")
	ResponseEntity<List<GithubFile>> getGithubRepositoryContent(
		@PathVariable("repoOwnerAndName") String repoOwnerAndName,
		@PathVariable("path") String path
	);
}
