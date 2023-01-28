package software.uncharted.terarium.hmiserver.proxies.skema;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import software.uncharted.terarium.hmiserver.models.CodeRequest;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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
	Response getFunctionNetwork(CodeRequest request);
}
