package software.uncharted.terarium.hmiserver.controller;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
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
	public ResponseEntity<JsonNode> mathML2ACSet(List<String> list) {
		return skemaProxy.convertMathML2ACSet(list);
	}

	@PostMapping("/acset-to-latex")
	public ResponseEntity<JsonNode> acet2Latex(PetriNet content) {
		return modelServiceProxy.petrinetToLatex(content);
	}
}
