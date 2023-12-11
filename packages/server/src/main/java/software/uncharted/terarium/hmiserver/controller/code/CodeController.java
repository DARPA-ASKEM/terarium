package software.uncharted.terarium.hmiserver.controller.code;

import org.apache.commons.text.StringEscapeUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import lombok.extern.slf4j.Slf4j;
import software.uncharted.terarium.hmiserver.controller.services.DownloadService;
import software.uncharted.terarium.hmiserver.models.StoredModel;
import software.uncharted.terarium.hmiserver.models.code.CodeRequest;
import software.uncharted.terarium.hmiserver.models.code.GithubFile;
import software.uncharted.terarium.hmiserver.models.code.GithubRepo;
import software.uncharted.terarium.hmiserver.proxies.github.GithubProxy;
import software.uncharted.terarium.hmiserver.proxies.jsdelivr.JsDelivrProxy;
import software.uncharted.terarium.hmiserver.proxies.skema.SkemaProxy;
import software.uncharted.terarium.hmiserver.proxies.skema.SkemaRustProxy;
import software.uncharted.terarium.hmiserver.security.Roles;

import java.io.InputStream;
import java.util.List;

@Slf4j
@RequestMapping("/code")
@RestController
public class CodeController {
	@Autowired
	GithubProxy githubProxy;

	@Autowired
	JsDelivrProxy jsdelivrProxy;

	@Autowired
	SkemaProxy skemaProxy;

	@Autowired
	SkemaRustProxy skemaRustProxy;

	/**
	 * Stores a model from a code snippet
	 *
	 * @param code the python code snippet
	 * @return a {@link StoredModel} instance containing the model id, inputs, and outputs of the model
	 * derived from the code input
	 */
	@PostMapping
	@Secured(Roles.USER)
	public ResponseEntity<StoredModel> transformCode(final String code) {

		// Convert from highlighted code a function network
		final String skemaResponseStr = skemaProxy.getFunctionNetwork(new CodeRequest(code)).getBody();

		if (skemaResponseStr == null || skemaResponseStr.isEmpty()) {
			return ResponseEntity.noContent().build();
		}

		// The model is returned from Skema as escaped, quoted json.  Eg:
		// "{\"hello\": \"world\" .... }"
		// We must remove the leading and trailing quotes, and un-escape the json in order to pass it on to the
		// service that will store the model as it expects application/json and not a string
		final String unesescapedSkemaResponseStr = StringEscapeUtils.unescapeJson(skemaResponseStr.substring(1, skemaResponseStr.length() - 1));

		// Store the model
		final String modelId = skemaRustProxy.addModel(unesescapedSkemaResponseStr).getBody();

		final String odiResponseStr = skemaRustProxy.getModelNamedOpis(modelId).getBody();
		final String odoResponseStr = skemaRustProxy.getModelNamedOpos(modelId).getBody();

		return ResponseEntity.ok(new StoredModel()
			.setId(modelId)
			.setInputs(odiResponseStr)
			.setOutputs(odoResponseStr)
		);
	}

	@GetMapping("/repo-content")
	@Secured(Roles.USER)
	public ResponseEntity<GithubRepo> getGithubRepositoryContent(
		@RequestParam("repoOwnerAndName") final String repoOwnerAndName,
		@RequestParam("path") final String path
	) {
		List<GithubFile> files = githubProxy.getGithubRepositoryContent(repoOwnerAndName, path).getBody();

		if (files == null || files.isEmpty()) {
			return ResponseEntity.noContent().build();
		}

		return ResponseEntity.ok(new GithubRepo(files));
	}

	@GetMapping("/repo-file-content")
	@Secured(Roles.USER)
	public ResponseEntity<String> getGithubCode(
		@RequestParam("repoOwnerAndName") final String repoOwnerAndName,
		@RequestParam("path") final String path
	) {
		return ResponseEntity.ok(jsdelivrProxy.getGithubCode(repoOwnerAndName, path).getBody());
	}

	@GetMapping("/repo-zip")
	public ResponseEntity<byte[]> downloadGitHubRepositoryZip(
		@RequestParam("repoOwnerAndName") final String repoOwnerAndName
	) {

		try(final CloseableHttpClient httpClient = HttpClients.custom()
			.build()) {

			String githubApiUrl = "https://api.github.com/repos/" + repoOwnerAndName + "/zipball/";

			HttpGet httpGet = new HttpGet(githubApiUrl);
            HttpResponse response = httpClient.execute(httpGet);

			final byte[] zipBytes = response.getEntity().getContent().readAllBytes();
			return ResponseEntity.ok(zipBytes);

		} catch (Exception e){
			log.error(e.toString());
			return ResponseEntity.internalServerError().build();
		}

	}
	
}
