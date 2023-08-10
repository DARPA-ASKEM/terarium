package software.uncharted.terarium.hmiserver.proxies.mit;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import software.uncharted.terarium.hmiserver.annotations.LogRestClientTime;
import software.uncharted.terarium.hmiserver.models.modelservice.PetriNet;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;
import java.util.List;

@RegisterRestClient(configKey = "mit")
public interface MitProxy {
	@POST
	@Path("/petri/get_places")
	@LogRestClientTime
	String getPlaces(
		@QueryParam("code") final String code);

	@POST
	@Path("/petri/get_transitions")
	@LogRestClientTime
	String getTransitions(
		@QueryParam("code") final String code);

	@POST
	@Path("/petri/get_arcs")
	@LogRestClientTime
	String getArcs(
		@QueryParam("code") final String code);

	@POST
	@Path("/petri/get_pyacset")
	@LogRestClientTime
	String getPyAcset(
		@QueryParam("places_str") final String places,
		@QueryParam("transitions_str") final String transitions,
		@QueryParam("arcs_str") final String arcs
	);

	@POST
	@Path("/annotation/find_text_vars")
	@LogRestClientTime
	String findTextVars(
		@QueryParam("async") final String async,
		@QueryParam("text") final String text
	);

	@POST
	@Path("/annotation/link_annos_to_pyacset")
	@LogRestClientTime
	String linkAnnotationsToAcset(
		@QueryParam("pyacset_str") final String pyacset,
		@QueryParam("annotations_str") final String annotations,
		@QueryParam("info_str") final String info
	);

	@POST
	@Path("/annotation/link_dataset_col_to_dkg")
	@LogRestClientTime
	String linkDatasetColToDKG(
		@QueryParam("csv_str") final String csv,
		@QueryParam("doc") final String doc
	);

	@GET
	@Path("/response")
	@LogRestClientTime
	String getResponse(@QueryParam("id") final String id);
}
