package software.uncharted.terarium.hmiserver.resources.miraservice;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import software.uncharted.terarium.hmiserver.exceptions.HmiResponseExceptionMapper;
import software.uncharted.terarium.hmiserver.models.dataservice.Model;
import software.uncharted.terarium.hmiserver.models.mira.DKG;
import software.uncharted.terarium.hmiserver.proxies.mira.MIRAProxy;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.net.URLEncoder;
import java.util.List;

@Path("/api/mira")
@Tag(name = "MIRA REST Endpoints")
@Produces(MediaType.APPLICATION_JSON)
@Slf4j
public class MIRAResource {
	@Inject
	@RestClient
	MIRAProxy proxy;

	@GET
	@Path("/{curies}")
	public List<DKG> searchConcept(
		@PathParam("curies") final String curies
	) {
		try {
			return  proxy.getEntities(curies);
		} catch (RuntimeException e) {
			log.error("Unable to fetch DKG", e);
			return null;
		}
	}

	// This rebuilds the semantics ODE via MIRA
	// 1. Cache semantics.typing and semantics.span data
	// 2. Send AMR to MIRA => MIRANet
	// 3. Send MIRANet to MIRA to convert back to AMR Petrinet
	// 4. Reattach semantics.typing and semantics.span
	// 5. Send back to client
	@POST
	@Path("/reconstruct_ode_semantics")
	public Object reconstructODESemantics(
			Model amr
	) {
		Object result = proxy.reconstructODESemantics(amr);
		Model amrReconstructed = proxy.toPetrinet(result);

		return amrReconstructed;
	}

}
