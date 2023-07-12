
package software.uncharted.terarium.hmiserver.proxies.extractionservice;

import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import javax.ws.rs.core.MediaType;
import software.uncharted.terarium.hmiserver.exceptions.HmiResponseExceptionMapper;

import java.util.List;
import java.util.Map;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

@RegisterRestClient(configKey = "extraction")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Extraction Service")
@RegisterProvider(HmiResponseExceptionMapper.class)
public interface ExtractionServiceProxy {

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	Map<String, Object> postMathMLToAMR(List<String> payload, @QueryParam("model") String model);

	@GET
	@Path("/status/{simulationId}")
	Response getTaskStatus(@PathParam("simulationId") String taskId);
}
