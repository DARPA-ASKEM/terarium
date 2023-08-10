package software.uncharted.terarium.hmiserver.proxies.skema;

import com.fasterxml.jackson.databind.JsonNode;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import software.uncharted.terarium.hmiserver.annotations.LogRestClientTime;
import software.uncharted.terarium.hmiserver.models.dataservice.Model;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

@RegisterRestClient(configKey = "skema-unified")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "SKEMA Unified Service")
public interface SkemaUnifiedProxy {

	/**
	 * Converts a LaTeX equation to an AMR of different framework via TA1 Skema-unified.
	 * @param request	the Json containing the LaTeX equations and model framework
	 *                https://skema-unified.staging.terarium.ai/docs#/workflows/equations_to_amr_workflows_latex_equations_to_amr_post
	 *                ie: { "equations": [ "equation1", "equation2", ... ], "model": "petrinet" }
	 * @return AMR Model
	 */
	@POST
	@Path("/workflows/latex/equations-to-amr")
	@LogRestClientTime
	@Consumes(MediaType.APPLICATION_JSON)
	Model postLaTeXToAMR(JsonNode request);
}
