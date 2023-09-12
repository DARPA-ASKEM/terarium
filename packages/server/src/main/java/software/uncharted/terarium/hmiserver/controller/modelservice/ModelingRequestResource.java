package software.uncharted.terarium.hmiserver.controller.modelservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import software.uncharted.terarium.hmiserver.models.modelservice.StratifyRequest;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import software.uncharted.terarium.hmiserver.proxies.modelservice.ModelServiceProxy;

@RequestMapping("/modeling-request")
@RestController
@Slf4j
public class ModelingRequestResource {

	@Autowired 
	private ModelServiceProxy modelServiceProxy;

	@PostMapping("/stratify")
	public ResponseEntity<Object> stratify(
		@RequestBody final StratifyRequest req
	) {
		return modelServiceProxy.stratify(req);
	}
}

