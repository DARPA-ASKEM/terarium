package software.uncharted.terarium.hmiserver.proxies.skema;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@RegisterRestClient(configKey = "skema-rust")
public interface SkemaRustProxy {
	/**
	 * Store the model
	 * @param functionNetwork	a JSON string of the function network
	 * @return	the model id of the stored model
	 */
	@POST
	@Path("/models")
	@Consumes(MediaType.APPLICATION_JSON)
	Response addModel(String functionNetwork);

	/**
	 * Gets a list of inputs from a stored model id
	 * @param modelId	the id of the stored model
	 * @return				the list of inputs
	 */
	@GET
	@Path("/models/{modelId}/named_opis")
	Response getModelNamedOpis(@PathParam("modelId") String modelId);

	/**
	 * Gets a list of outputs from a stored model id
	 * @param modelId	the id of the stored model
	 * @return				the list of outputs
	 */
	@GET
	@Path("/models/{modelId}/named_opos")
	Response getModelNamedOpos(@PathParam("modelId") String modelId);


	@PUT
	@Path("/mathml/acset")
	@Consumes(MediaType.APPLICATION_JSON)
	Response convertMathML2ACSet(List<String> mathML);
}
