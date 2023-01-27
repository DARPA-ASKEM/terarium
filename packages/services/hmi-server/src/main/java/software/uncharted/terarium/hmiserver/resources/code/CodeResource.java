package software.uncharted.terarium.hmiserver.resources.code;

import io.quarkus.security.Authenticated;
import org.apache.commons.text.StringEscapeUtils;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import software.uncharted.terarium.hmiserver.models.CodeRequest;
import software.uncharted.terarium.hmiserver.models.StoredModel;
import software.uncharted.terarium.hmiserver.proxies.SkemaProxy;
import software.uncharted.terarium.hmiserver.proxies.SkemaRustProxy;

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

	@POST
	public Response transformCode(final CodeRequest request) {

		// Convert from highlighted code a function network
		Response skemaResponse = skemaProxy.getFunctionNetwork(request);
		final String skemaResponseStr = skemaResponse.readEntity(String.class);
		final String unesescapedSkemaResponseStr = StringEscapeUtils.unescapeJson(skemaResponseStr.substring(1, skemaResponseStr.length()-1));

		// Store the model
		final Response addModelResponse = skemaRustProxy.addModel(unesescapedSkemaResponseStr);

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
