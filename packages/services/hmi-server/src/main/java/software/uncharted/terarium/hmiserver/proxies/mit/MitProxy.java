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
	List<List<String>> getPyAcset(@QueryParam("places") final String places,
																	 @QueryParam("transitions") final String transitions,
																	 @QueryParam("arcs") final String arcs,
																	 @QueryParam("gpt_key") final String key);
}
