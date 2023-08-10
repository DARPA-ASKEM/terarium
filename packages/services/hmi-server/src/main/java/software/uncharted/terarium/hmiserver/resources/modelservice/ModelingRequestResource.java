package software.uncharted.terarium.hmiserver.resources.modelservice;

import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import software.uncharted.terarium.hmiserver.models.modelservice.StratifyRequest;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import software.uncharted.terarium.hmiserver.proxies.modelservice.ModelServiceProxy;

@Path("/api/modeling-request")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Modeling Service REST Endpoint")
public class ModelingRequestResource {

	@RestClient
	ModelServiceProxy modelServiceProxy;

	@POST
	@Path("/stratify")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Tag(name = "Stratify two AMR models together")
	public Object stratify(
			final StratifyRequest req
	) {
		return modelServiceProxy.stratify(req);
	}
}

