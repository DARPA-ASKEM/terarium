package software.uncharted.terarium.hmiserver.resources.code;

import io.quarkus.security.Authenticated;
import org.apache.commons.text.StringEscapeUtils;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import software.uncharted.terarium.hmiserver.models.CodeRequest;
import software.uncharted.terarium.hmiserver.models.StoredModel;
import software.uncharted.terarium.hmiserver.proxies.github.GithubProxy;
import software.uncharted.terarium.hmiserver.proxies.jsdelivr.JsDelivrProxy;
import software.uncharted.terarium.hmiserver.proxies.mit.MitProxy;
import software.uncharted.terarium.hmiserver.proxies.skema.SkemaProxy;
import software.uncharted.terarium.hmiserver.proxies.skema.SkemaRustProxy;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;

@Path("/api/code")
@Authenticated
@Tag(name = "Code REST Endpoint")
@Produces(MediaType.APPLICATION_JSON)
public class CodeResource {
	@RestClient
	GithubProxy githubProxy;

	@RestClient
	JsDelivrProxy jsdelivrProxy;

	@RestClient
	MitProxy mitProxy;

	@RestClient
	SkemaProxy skemaProxy;

	@RestClient
	SkemaRustProxy skemaRustProxy;

	/**
	 * Stores a model from a code snippit
	 *
	 * @param code the python code snippit
	 * @return a {@link StoredModel} instance containing the model id, inputs, and outputs of the model
	 * derived from the code input
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
		final String unesescapedSkemaResponseStr = StringEscapeUtils.unescapeJson(skemaResponseStr.substring(1, skemaResponseStr.length() - 1));

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

	@POST
	@Path("/to-acset")
	public Object toAcset(final String code) {
		String places = mitProxy.getPlaces(code);
		String transitions = mitProxy.getTransitions(code);
		String arcs = mitProxy.getArcs(code);
		String pyAcset = mitProxy.getPyAcset(places, transitions, arcs);
		return Response.ok(Response.Status.OK)
			.entity(pyAcset)
			.build();
	}

	@POST
	@Path("/annotation/find-text-vars")
	public Object findTextVars(final String text) {
		String textVars = mitProxy.findTextVars("true", text);
		return Response.ok(Response.Status.OK)
			.entity(textVars)
			.build();
	}

	@POST
	@Path("/annotation/link-annos-to-pyacset")
	public Object linkAnnotationsToAcset(final Map<String, String> data) {
		String pyacset = data.get("pyacset");
		String annotations = data.get("annotations");
		String info = data.get("info");
		String metadata = mitProxy.linkAnnotationsToAcset(pyacset, annotations, info);
		return Response.ok(Response.Status.OK)
			.entity(metadata)
			.build();
	}

	@GET
	@Path("/response")
	public Object getResponse(@QueryParam("id") final String id) {
		String response = mitProxy.getResponse(id);
		return Response.ok(Response.Status.OK)
			.entity(response)
			.build();
	}

	@GET
	@Path("/repo-content")
	public Response getGithubRepositoryContent(
		@QueryParam("repoOwnerAndName") final String repoOwnerAndName,
		@QueryParam("path") final String path
	) {
		return githubProxy.getGithubRepositoryContent(repoOwnerAndName, path);
	}

	@GET
	@Path("/repo-file-content")
	public String getGithubCode(
		@QueryParam("repoOwnerAndName") final String repoOwnerAndName,
		@QueryParam("path") final String path
	) {
		return jsdelivrProxy.getGithubCode(repoOwnerAndName, path);
	}
}
