package software.uncharted.terarium.hmiserver.controller.code;

import feign.FeignException;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import software.uncharted.terarium.hmiserver.models.code.GithubFile;
import software.uncharted.terarium.hmiserver.models.code.GithubRepo;
import software.uncharted.terarium.hmiserver.proxies.github.GithubProxy;
import software.uncharted.terarium.hmiserver.proxies.jsdelivr.JsDelivrProxy;
import software.uncharted.terarium.hmiserver.security.Roles;

@Slf4j
@RequestMapping("/code")
@RestController
@RequiredArgsConstructor
public class CodeController {

	private final GithubProxy githubProxy;

	private final JsDelivrProxy jsdelivrProxy;

	@GetMapping("/repo-content")
	@Secured(Roles.USER)
	@ApiResponses(
		value = {
			@ApiResponse(responseCode = "200", description = "Github repository content retrieved"),
			@ApiResponse(responseCode = "204", description = "No content")
		}
	)
	public ResponseEntity<GithubRepo> getGithubRepositoryContent(
		@RequestParam("repo-owner-and-name") final String repoOwnerAndName,
		@RequestParam("path") final String path
	) {
		final List<GithubFile> files;
		try {
			files = githubProxy.getGithubRepositoryContent(repoOwnerAndName, path).getBody();
		} catch (final FeignException e) {
			final String error = "Error getting github repository content";
			final int status = e.status() >= 400 ? e.status() : 500;
			log.error(error, e);
			throw new ResponseStatusException(org.springframework.http.HttpStatus.valueOf(status), error);
		}

		if (files == null || files.isEmpty()) {
			return ResponseEntity.noContent().build();
		}

		return ResponseEntity.ok(new GithubRepo(files));
	}

	@GetMapping("/repo-file-content")
	@Secured(Roles.USER)
	public ResponseEntity<String> getGithubCode(
		@RequestParam("repo-owner-and-name") final String repoOwnerAndName,
		@RequestParam("path") final String path
	) {
		final String code;
		try {
			code = jsdelivrProxy.getGithubCode(repoOwnerAndName, path).getBody();
		} catch (final FeignException e) {
			final String error = "Error getting github code";
			final int status = e.status() >= 400 ? e.status() : 500;
			log.error(error, e);
			throw new ResponseStatusException(org.springframework.http.HttpStatus.valueOf(status), error);
		}

		return ResponseEntity.ok(code);
	}

	@GetMapping("/repo-zip")
	public ResponseEntity<byte[]> downloadGitHubRepositoryZip(
		@RequestParam("repo-owner-and-name") final String repoOwnerAndName
	) {
		try (final CloseableHttpClient httpClient = HttpClients.custom().build()) {
			final String githubApiUrl = "https://api.github.com/repos/" + repoOwnerAndName + "/zipball/";

			final HttpGet httpGet = new HttpGet(githubApiUrl);
			final HttpResponse response = httpClient.execute(httpGet);

			final byte[] zipBytes = response.getEntity().getContent().readAllBytes();
			return ResponseEntity.ok(zipBytes);
		} catch (final Exception e) {
			log.error(e.toString());
			throw new ResponseStatusException(
				org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
				"Unable to download zip file from github"
			);
		}
	}
}
