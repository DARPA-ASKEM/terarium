package software.uncharted.terarium.hmiserver.proxies.modelservice;

import software.uncharted.terarium.hmiserver.models.modelservice.StratifyRequest;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import software.uncharted.terarium.hmiserver.models.modelservice.PetriNet;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@FeignClient(name = "model-service", url = "${terarium.dataservice.url}", path = "/model-service")
public interface ModelServiceProxy {
	@PostMapping("/petri-to-latex")
	ResponseEntity<Response> petrinetToLatex(
		@RequestBody PetriNet content
	);

	@PostMapping("/stratify")
	ResponseEntity<Response> stratify(
		@RequestBody StratifyRequest req
	);

}
