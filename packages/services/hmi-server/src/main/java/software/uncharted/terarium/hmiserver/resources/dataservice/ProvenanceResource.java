package software.uncharted.terarium.hmiserver.resources.dataservice;

import io.quarkus.security.Authenticated;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import software.uncharted.terarium.hmiserver.proxies.dataservice.ProvenanceProxy;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
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
}
