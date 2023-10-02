package software.uncharted.terarium.hmiserver.controller.miraservice;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import software.uncharted.terarium.hmiserver.models.mira.DKG;
import software.uncharted.terarium.hmiserver.proxies.mira.MIRAProxy;

import java.util.List;


@RequestMapping("/mira")
@RestController
@Slf4j
public class MIRAResource {
	@Autowired
	MIRAProxy proxy;

	@GetMapping("/{curies}")
	public ResponseEntity<List<DKG>> searchConcept(
		@PathVariable("curies") final String curies
	) {
		try {
			return ResponseEntity.ok(proxy.getEntities(curies)).getBody();
		} catch (RuntimeException e) {
			log.error("Unable to fetch DKG", e);
			return ResponseEntity.internalServerError().build();
		}
	}

	// This rebuilds the semantics ODE via MIRA
	// 1. Send AMR to MIRA => MIRANet
	// 2. Send MIRANet to MIRA to convert back to AMR Petrinet
	// 3. Send AMR back
	@PostMapping("/reconstruct_ode_semantics")
	public ResponseEntity<JsonNode> reconstructODESemantics(
		Object amr
	) {
		return ResponseEntity.ok(proxy.reconstructODESemantics(amr).getBody());

	}
}
