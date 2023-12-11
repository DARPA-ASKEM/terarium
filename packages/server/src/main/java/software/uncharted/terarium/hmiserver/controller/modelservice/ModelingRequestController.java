package software.uncharted.terarium.hmiserver.controller.modelservice;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import software.uncharted.terarium.hmiserver.models.modelservice.StratifyRequest;
import software.uncharted.terarium.hmiserver.proxies.modelservice.ModelServiceProxy;
import software.uncharted.terarium.hmiserver.security.Roles;

@RequestMapping("/modeling-request")
@RestController
@Slf4j
public class ModelingRequestController {

	@Autowired
	private ModelServiceProxy modelServiceProxy;

	@PostMapping("/stratify")
	@Secured(Roles.USER)
	public ResponseEntity<JsonNode> stratify(
		@RequestBody final StratifyRequest req
	) {
		return ResponseEntity.ok(modelServiceProxy.stratify(req).getBody());
	}
}

