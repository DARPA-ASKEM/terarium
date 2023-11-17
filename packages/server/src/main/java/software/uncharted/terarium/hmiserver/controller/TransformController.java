package software.uncharted.terarium.hmiserver.controller;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import software.uncharted.terarium.hmiserver.models.modelservice.PetriNet;
import software.uncharted.terarium.hmiserver.proxies.modelservice.ModelServiceProxy;
import software.uncharted.terarium.hmiserver.proxies.skema.SkemaRustProxy;
import software.uncharted.terarium.hmiserver.security.Roles;

import java.util.List;


@RequestMapping("/transforms")
@RestController
@Slf4j
public class TransformController {

	@Autowired
	SkemaRustProxy skemaProxy;

	@Autowired
	ModelServiceProxy modelServiceProxy;

	@PostMapping("/mathml-to-acset")
	@Secured(Roles.USER)
	public ResponseEntity<JsonNode> mathML2ACSet(@RequestBody final List<String> list) {
		return skemaProxy.convertMathML2ACSet(list);
	}

	@PostMapping(value = "/acset-to-latex", produces = {"text/plain", "application/*"})
	@Secured(Roles.USER)
	public ResponseEntity<String> acset2Latex(@RequestBody final PetriNet content) {
		final ResponseEntity<String> res = modelServiceProxy.petrinetToLatex(content);

		// since the model-service returns headers that are duplicated in the hmi-server response,
		// we need to strip them out. This stops our nginx reverse proxy from thinking that there
		// is an HTTP smuggling attack.
		return ResponseEntity.status(res.getStatusCode()).body(res.getBody());
	}
}
