package software.uncharted.terarium.hmiserver.resources.miraservice;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import software.uncharted.terarium.hmiserver.exceptions.HmiResponseExceptionMapper;
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
}
