package software.uncharted.terarium.hmiserver.controller;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import software.uncharted.terarium.hmiserver.models.modelservice.PetriNet;
import software.uncharted.terarium.hmiserver.proxies.modelservice.ModelServiceProxy;
import software.uncharted.terarium.hmiserver.proxies.skema.SkemaRustProxy;

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
	public ResponseEntity<JsonNode> mathML2ACSet(@RequestBody final List<String> list) {
		return skemaProxy.convertMathML2ACSet(list);
	}

	@PostMapping(value = "/acset-to-latex", produces = {"text/plain", "application/*"})
	public ResponseEntity<String> acet2Latex(@RequestBody final PetriNet content) {
		final ResponseEntity<String> proxyRes = modelServiceProxy.petrinetToLatex(content);

		// since the model service returns headers that are duplicated in the hmi-server response,
		// we need to strip them out. This stops our nginx reverse proxy from thinking that there
		// is an HTTP smuggling attack.
		return new ResponseEntity<>(proxyRes.getBody(), proxyRes.getStatusCode());
	}
}
