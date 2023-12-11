package software.uncharted.terarium.hmiserver.controller.dataservice;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import software.uncharted.terarium.hmiserver.controller.SnakeCaseController;
import software.uncharted.terarium.hmiserver.models.dataservice.Workflow;
import software.uncharted.terarium.hmiserver.proxies.dataservice.WorkflowProxy;
import software.uncharted.terarium.hmiserver.security.Roles;

import java.util.List;

@RequestMapping("/workflows")
@RestController
@Slf4j
public class WorkflowController implements SnakeCaseController {

	@Autowired
	WorkflowProxy workflowProxy;

	@GetMapping
	@Secured(Roles.USER)
	public ResponseEntity<List<Workflow>> getWorkflows(
		@RequestParam(name = "page_size", defaultValue = "100", required = false) final Integer pageSize,
		@RequestParam(name = "page", defaultValue = "0", required = false) final Integer page
	) {
		return ResponseEntity.ok(workflowProxy.getAssets(pageSize, page).getBody());
	}

	@GetMapping("/{id}")
	@Secured(Roles.USER)
	public ResponseEntity<Workflow> getWorkflow(
		@PathVariable("id") String id
	) {
		return ResponseEntity.ok(workflowProxy.getAsset(id).getBody());
	}

	@PostMapping
	@Secured(Roles.USER)
	public ResponseEntity<JsonNode> createWorkflow(@RequestBody Workflow item) {
		return ResponseEntity.ok(workflowProxy.createAsset(convertObjectToSnakeCaseJsonNode(item)).getBody());
	}

	@PutMapping("/{id}")
	@Secured(Roles.USER)
	public ResponseEntity<JsonNode> updateWorkflow(
		@PathVariable("id") String id,
		@RequestBody Workflow workflow
	) {
		return ResponseEntity.ok(workflowProxy.updateAsset(id, convertObjectToSnakeCaseJsonNode(workflow)).getBody());
	}
}

