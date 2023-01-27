package software.uncharted.terarium.hmiserver.proxies;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.keycloak.adapters.spi.HttpFacade;
import software.uncharted.terarium.hmiserver.models.CodeRequest;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@RegisterRestClient(configKey = "skema")
public interface SkemaProxy {
	@POST
	@Path("/fn-given-filepaths")
	@Produces(MediaType.APPLICATION_JSON)
	Response getFunctionNetwork(CodeRequest request);
}
