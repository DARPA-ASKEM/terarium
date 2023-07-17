package software.uncharted.terarium.hmiserver.proxies.skema;

import com.fasterxml.jackson.databind.JsonNode;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import software.uncharted.terarium.hmiserver.annotations.LogRestClientTime;
import software.uncharted.terarium.hmiserver.models.code.CodeRequest;
import software.uncharted.terarium.hmiserver.models.dataservice.Model;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@RegisterRestClient(configKey = "skema-unified")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "SKEMA Unified Service")
public interface SkemaUnifiedProxy {

	/**
	 * Converts a LaTeX equation to an AMR of different framework via TA1 Skema-unified.
	 * @param request	the {@link CodeRequest} instance containing the LaTeX equations and model framework
	 *                ie: { equations: [ "equation1", "equation2", ... ], framework: "framework" }
	 * @return AMR Model
	 */
	@POST
	@Path("/workflows/latex/equations-to-amr")
	@LogRestClientTime
	@Consumes(MediaType.APPLICATION_JSON)
	Model postLaTeXToAMR(JsonNode request);
}
