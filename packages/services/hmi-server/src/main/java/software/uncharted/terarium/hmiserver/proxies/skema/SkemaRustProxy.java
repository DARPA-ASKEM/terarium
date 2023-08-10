package software.uncharted.terarium.hmiserver.proxies.skema;

import com.fasterxml.jackson.databind.JsonNode;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import software.uncharted.terarium.hmiserver.annotations.LogRestClientTime;

import java.util.List;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

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
	@LogRestClientTime
	Response addModel(String functionNetwork);

	/**
	 * Gets a list of inputs from a stored model id
	 * @param modelId	the id of the stored model
	 * @return				the list of inputs
	 */
	@GET
	@Path("/models/{modelId}/named_opis")
	@LogRestClientTime
	Response getModelNamedOpis(@PathParam("modelId") String modelId);

	/**
	 * Gets a list of outputs from a stored model id
	 * @param modelId	the id of the stored model
	 * @return				the list of outputs
	 */
	@GET
	@Path("/models/{modelId}/named_opos")
	@LogRestClientTime
	Response getModelNamedOpos(@PathParam("modelId") String modelId);


	@PUT
	@Path("/mathml/acset")
	@Consumes(MediaType.APPLICATION_JSON)
	@LogRestClientTime
	Response convertMathML2ACSet(List<String> mathML);

	@PUT
	@Path("/mathml/amr")
	@Consumes(MediaType.APPLICATION_JSON)
	@LogRestClientTime
	Response convertMathML2AMR(JsonNode request);
}
