package software.uncharted.terarium.hmiserver.resources.dataservice;

import io.quarkus.security.Authenticated;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import software.uncharted.terarium.hmiserver.models.dataservice.Provenance;
import software.uncharted.terarium.hmiserver.proxies.dataservice.ProvenanceProxy;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/api/provenance")
@Authenticated
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Provenance REST Endpoints")
public class ProvenanceResource {

	@Inject
	@RestClient
	ProvenanceProxy proxy;

	@GET
	public Response getProvenance() {
		return proxy.getProvenance();
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createProvenance(
		final Provenance provenance
	) {
		return proxy.createProvenance(provenance);
	}

	@GET
	@Path("/derived_from")
	public Response searchProvenance(
		@QueryParam("artifact_id") final String artifactId,
		@QueryParam("artifact_type") final String artifactType
	) {
		return proxy.searchProvenance(artifactId, artifactType);
	}

	@DELETE
	@Path("/hanging_nodes")
	public Response deleteHangingNodes() {
		return proxy.deleteHangingNodes();
	}

	@DELETE
	@Path("/{id}")
	public Response deleteProvenance(
		@PathParam("id") final String id
	) {
		return proxy.deleteProvenance(id);
	}
}
