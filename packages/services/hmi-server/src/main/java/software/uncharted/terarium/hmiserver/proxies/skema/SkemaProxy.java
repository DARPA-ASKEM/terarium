package software.uncharted.terarium.hmiserver.proxies.skema;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import software.uncharted.terarium.hmiserver.annotations.LogRestClientTime;
import software.uncharted.terarium.hmiserver.models.code.CodeRequest;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@RegisterRestClient(configKey = "skema")
public interface SkemaProxy {
	/**
	 * Converts a {@link CodeRequest} to a function network via TA1 Skema
	 * @param request	the {@link CodeRequest} instance containing the code snippit
	 * @return	an escaped JSON string of the function network
	 */
	@POST
	@Path("/fn-given-filepaths")
	@Produces(MediaType.APPLICATION_JSON)
	@LogRestClientTime
	Response getFunctionNetwork(CodeRequest request);
}
