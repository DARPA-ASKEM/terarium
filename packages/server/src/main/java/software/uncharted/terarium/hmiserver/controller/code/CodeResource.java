package software.uncharted.terarium.hmiserver.controller.code;

import org.apache.commons.text.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import software.uncharted.terarium.hmiserver.models.code.CodeRequest;
import software.uncharted.terarium.hmiserver.models.StoredModel;
import software.uncharted.terarium.hmiserver.models.code.GithubFile;
import software.uncharted.terarium.hmiserver.models.code.GithubRepo;
import software.uncharted.terarium.hmiserver.proxies.github.GithubProxy;
import software.uncharted.terarium.hmiserver.proxies.jsdelivr.JsDelivrProxy;
import software.uncharted.terarium.hmiserver.proxies.mit.MitProxy;
import software.uncharted.terarium.hmiserver.proxies.skema.SkemaProxy;
import software.uncharted.terarium.hmiserver.proxies.skema.SkemaRustProxy;


import java.util.List;
import java.util.Map;

@RequestMapping("/code")
@RestController
public class CodeResource {
	@Autowired
	GithubProxy githubProxy;

	@Autowired
	JsDelivrProxy jsdelivrProxy;

	@Autowired
	MitProxy mitProxy;

	@Autowired
	SkemaProxy skemaProxy;

	@Autowired
	SkemaRustProxy skemaRustProxy;

	/**
	 * Stores a model from a code snippit
	 *
	 * @param code the python code snippit
	 * @return a {@link StoredModel} instance containing the model id, inputs, and outputs of the model
	 * derived from the code input
	 */
	@PostMapping
	public ResponseEntity<StoredModel> transformCode(final String code) {

		// Convert from highlighted code a function network
		final String skemaResponseStr  = skemaProxy.getFunctionNetwork(new CodeRequest(code)).getBody();

		if(skemaResponseStr == null || skemaResponseStr.isEmpty()) {
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
		final String odoResponseStr =  skemaRustProxy.getModelNamedOpos(modelId).getBody();

		return ResponseEntity.ok(new StoredModel()
			.setId(modelId)
			.setInputs(odiResponseStr)
			.setOutputs(odoResponseStr)
		);
	}

	@PostMapping("/to-acset")
	public ResponseEntity<String> toAcset(final String code) {
		String places = mitProxy.getPlaces(code).getBody();
		String transitions = mitProxy.getTransitions(code).getBody();
		String arcs = mitProxy.getArcs(code).getBody();
		String pyAcset = mitProxy.getPyAcset(places, transitions, arcs).getBody();
		return ResponseEntity.ok(pyAcset);
	}

	@PostMapping("/annotation/find-text-vars")
	public ResponseEntity<String> findTextVars(final String text) {
		String textVars = mitProxy.findTextVars("true", text).getBody();
		return ResponseEntity.ok(textVars);
	}

	@PostMapping("/annotation/link-annos-to-pyacset")
	public ResponseEntity<String> linkAnnotationsToAcset(@RequestBody final Map<String, String> data) {
		String pyacset = data.get("pyacset");
		String annotations = data.get("annotations");
		String info = data.get("info");
		String metadata = mitProxy.linkAnnotationsToAcset(pyacset, annotations, info).getBody();

		return ResponseEntity.ok(metadata);
	}

	@GetMapping("/response")
	public ResponseEntity<Object> getResponse(@RequestParam("id") final String id) {
		String response = mitProxy.getResponse(id).getBody();
		return ResponseEntity.ok(response);
	}

	@GetMapping("/repo-content")
	public ResponseEntity<GithubRepo> getGithubRepositoryContent(
		@RequestParam("repoOwnerAndName") final String repoOwnerAndName,
		@RequestParam("path") final String path
	) {
		List<GithubFile> files =  githubProxy.getGithubRepositoryContent(repoOwnerAndName, path).getBody();

		if(files == null || files.isEmpty()) {
			return ResponseEntity.noContent().build();
		}

		return ResponseEntity.ok(new GithubRepo(files));
	}

	@GetMapping("/repo-file-content")
	public ResponseEntity<String> getGithubCode(
		@RequestParam("repoOwnerAndName") final String repoOwnerAndName,
		@RequestParam("path") final String path
	) {
		return ResponseEntity.ok(jsdelivrProxy.getGithubCode(repoOwnerAndName, path).getBody());
	}
}
