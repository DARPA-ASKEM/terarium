package software.uncharted.terarium.hmiserver.resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import software.uncharted.terarium.hmiserver.models.modelservice.PetriNet;
import software.uncharted.terarium.hmiserver.proxies.skema.SkemaRustProxy;
import software.uncharted.terarium.hmiserver.proxies.modelservice.ModelServiceProxy;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.util.List;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;


@Path("/api/transforms")
@Slf4j
public class TransformResource {
	@Inject
	@RestClient
	SkemaRustProxy skemaProxy;

	@Inject
	@RestClient
	ModelServiceProxy modelServiceProxy;

	@POST
	@Path("/mathml-to-acset")
	public Response mathML2ACSet(List<String> list) {
		return skemaProxy.convertMathML2ACSet(list);
	}

	@POST
	@Path("/mathml-to-amr/{framework}")
	@APIResponses({
		@APIResponse(responseCode = "500", description = "An error occurred retrieving the AMR"),
		@APIResponse(responseCode = "400", description = "Query must contain a valid MathML and a Model framework")
	})
	public Response mathML2AMR(
		final List<String> mathml,
		@PathParam("framework") final String framework
	) {

		// Check if we have a framework and a MathML
		if (framework == null || mathml == null || mathml.isEmpty()) {
			return Response.status(Response.Status.BAD_REQUEST).build();
		}

		try {
			// Create Request Body
			// https://skema-rs.terarium.ai/docs/#/skema%3A%3Aservices%3A%3Amathml/get_amr
			ObjectMapper mapper = new ObjectMapper();
			ObjectNode request = mapper.createObjectNode();
			request.put("model", framework);
			request.put("mathml", mapper.valueToTree(mathml));
			Response response = skemaProxy.convertMathML2AMR(request);

			if (response.getStatus() != Response.Status.OK.ordinal()) {
				Response.status(Response.Status.BAD_REQUEST).build();
			}

			return response;

		} catch (RuntimeException e) {
			log.error("Unable to create an AMR", e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@POST
	@Path("/acset-to-latex")
	public Response acet2Latex(PetriNet content) {
		return modelServiceProxy.petrinetToLatex(content);
	}
}
