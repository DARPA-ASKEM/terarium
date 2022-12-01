package software.uncharted.terarium.hmiserver.proxies.dataservice;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import software.uncharted.terarium.hmiserver.models.dataservice.Provenance;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@RegisterRestClient(configKey = "data-service")
@Path("/provenance")
@Produces(MediaType.APPLICATION_JSON)
public interface ProvenanceProxy {
	@GET
	Response getProvenance();

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	Response createProvenance(
		Provenance provenance
	);

	@GET
	@Path("/derived_from")
	Response searchProvenance(
		@QueryParam("artifact_id") String artifactId,
		@QueryParam("artifact_type") String artifactType
	);

	@DELETE
	@Path("/hanging_nodes")
	Response deleteHangingNodes();

	@DELETE
	@Path("/{id}")
	Response deleteProvenance(
		@PathParam("id") String id
	);
}
