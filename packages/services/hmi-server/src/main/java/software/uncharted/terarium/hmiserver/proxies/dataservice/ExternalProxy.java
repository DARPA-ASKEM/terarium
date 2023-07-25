package software.uncharted.terarium.hmiserver.proxies.dataservice;

import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import software.uncharted.terarium.hmiserver.annotations.LogRestClientTime;
import software.uncharted.terarium.hmiserver.exceptions.HmiResponseExceptionMapper;
import software.uncharted.terarium.hmiserver.models.dataservice.DocumentAsset;
import software.uncharted.terarium.hmiserver.models.dataservice.Software;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@RegisterRestClient(configKey = "data-service")
@Path("/external")
@Produces(MediaType.APPLICATION_JSON)
@RegisterProvider(HmiResponseExceptionMapper.class)
public interface ExternalProxy {

	@GET
	@Path("/software/{id}")
	@LogRestClientTime
	Response getSoftware(
		@PathParam("id") String id
	);

	@DELETE
	@Path("/software/{id}")
	@LogRestClientTime
	Response deleteSoftware(
		@PathParam("id") String id
	);

	@POST
	@Path("/software")
	@Consumes(MediaType.APPLICATION_JSON)
	@LogRestClientTime
	Response createSoftware(
		Software software
	);

	@GET
	@Path("/publications/{id}")
	@LogRestClientTime
	Response getPublication(
		@PathParam("id") String id
	);

	@DELETE
	@Path("/publications/{id}")
	@LogRestClientTime
	Response deletePublication(
		@PathParam("id") String id
	);

	@POST
	@Path("/publications")
	@Consumes(MediaType.APPLICATION_JSON)
	@LogRestClientTime
	Response createPublication(
		DocumentAsset publication
	);
}
