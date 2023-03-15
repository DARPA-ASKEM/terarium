package software.uncharted.terarium.hmiserver.proxies.mit;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import java.util.List;

@RegisterRestClient(configKey = "mit")
public interface MitProxy {
	@POST
	@Path("/petri/get_places")
	List<String> getPetriPlaces(@QueryParam("code") final String code, @QueryParam("gpt_key") final String key);

	@POST
	@Path("/petri/get_transitions")
	List<String> getPetriTransitions(@QueryParam("code") final String code, @QueryParam("gpt_key") final String key);
}
