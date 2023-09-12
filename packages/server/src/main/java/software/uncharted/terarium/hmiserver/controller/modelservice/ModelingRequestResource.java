package software.uncharted.terarium.hmiserver.controller.modelservice;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import software.uncharted.terarium.hmiserver.models.modelservice.StratifyRequest;

import software.uncharted.terarium.hmiserver.proxies.modelservice.ModelServiceProxy;

@RequestMapping("/modeling-request")
@RestController
@Slf4j
public class ModelingRequestResource {

	@Autowired
	private ModelServiceProxy modelServiceProxy;

	@PostMapping("/stratify")
	public ResponseEntity<JsonNode> stratify(
		@RequestBody final StratifyRequest req
	) {
		return modelServiceProxy.stratify(req);
	}
}

