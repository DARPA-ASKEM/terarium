package software.uncharted.terarium.hmiserver.proxies.jsdelivr;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "jsdeliver", url = "${jsdelivr.url}")
public interface JsDelivrProxy {
	@GetMapping("/gh/{repoOwnerAndName}@latest/{path}")
	ResponseEntity<String> getGithubCode(
		@PathVariable("repoOwnerAndName") String repoOwnerAndName,
		@PathVariable("path") String path
	);
}
