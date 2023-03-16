package software.uncharted.terarium.hmiserver.proxies.mit;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import software.uncharted.terarium.hmiserver.models.modelservice.PetriNet;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.util.List;

@RegisterRestClient(configKey = "mit")
public interface MitProxy {
	@POST
	@Path("/petri/get_places")
	List<String> getPlaces(
		@QueryParam("code") final String code,
		@QueryParam("gpt_key") final String key);

	@POST
	@Path("/petri/get_transitions")
	List<String> getTransitions(
		@QueryParam("code") final String code,
		@QueryParam("gpt_key") final String key);

	@POST
	@Path("/petri/get_arcs")
	List<List<String>> getArcs(
		@QueryParam("code") final String code,
		@QueryParam("gpt_key") final String key);

	@POST
	@Path("/petri/get_pyacset")
	PetriNet getPyAcset(@QueryParam("places_str") final String places,
											@QueryParam("transitions_str") final String transitions,
											@QueryParam("arcs_str") final String arcs);
}
