package software.uncharted.terarium.hmiserver.resources.code;

import io.quarkus.security.Authenticated;
import org.apache.commons.text.StringEscapeUtils;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import software.uncharted.terarium.hmiserver.models.CodeRequest;
import software.uncharted.terarium.hmiserver.models.StoredModel;
import software.uncharted.terarium.hmiserver.proxies.skema.SkemaProxy;
import software.uncharted.terarium.hmiserver.proxies.skema.SkemaRustProxy;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Path("/api/code")
@Authenticated
@Tag(name = "Code REST Endpoint")
public class CodeResource {
	@RestClient
	SkemaProxy skemaProxy;

	@RestClient
	SkemaRustProxy skemaRustProxy;

	/**
	 * Stores a model from a code snippit
	 * @param code	the python code snippit
	 * @return			a {@link StoredModel} instance containing the model id, inputs, and outputs of the model
	 * 							derived from the code input
	 */
	@POST
	public Response transformCode(final String code) {

		// Convert from highlighted code a function network
		Response skemaResponse = skemaProxy.getFunctionNetwork(new CodeRequest(code));
		final String skemaResponseStr = skemaResponse.readEntity(String.class);

		// The model is returned from Skema as escaped, quoted json.  Eg:
		// "{\"hello\": \"world\" .... }"
		// We must remove the leading and trailing quotes, and un-escape the json in order to pass it on to the
		// service that will store the model as it expects application/json and not a string
		final String unesescapedSkemaResponseStr = StringEscapeUtils.unescapeJson(skemaResponseStr.substring(1, skemaResponseStr.length()-1));

		// Store the model
		final Response addModelResponse = skemaRustProxy.addModel(unesescapedSkemaResponseStr);

		// Prepare the response
		final String modelId = addModelResponse.readEntity(String.class);
		final Response odiResponse = skemaRustProxy.getModelNamedOpis(modelId);
		final String odiResponseStr = odiResponse.readEntity(String.class);
		final Response odoResponse = skemaRustProxy.getModelNamedOpos(modelId);
		final String odoResponseStr = odoResponse.readEntity(String.class);

		return Response.ok(new StoredModel()
			.setId(modelId)
			.setInputs(odiResponseStr)
			.setOutputs(odoResponseStr)
		).build();
	}
}
