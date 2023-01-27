package software.uncharted.terarium.hmiserver.proxies;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import software.uncharted.terarium.hmiserver.models.CodeRequest;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@RegisterRestClient(configKey = "skema-rust")
public interface SkemaRustProxy {
	@POST
	@Path("/models")
	@Consumes(MediaType.APPLICATION_JSON)
	Response addModel(String functionNetwork);

	@GET
	@Path("/models/{modelId}/named_opis")
	Response getModelNamedOpis(@PathParam("modelId") String modelId);

	@GET
	@Path("/models/{modelId}/named_opos")
	Response getModelNamedOpos(@PathParam("modelId") String modelId);
}
