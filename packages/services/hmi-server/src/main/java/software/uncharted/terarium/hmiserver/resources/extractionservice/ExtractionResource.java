package software.uncharted.terarium.hmiserver.resources.extractionservice;

import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import software.uncharted.terarium.hmiserver.proxies.extractionservice.ExtractionServiceProxy;
import software.uncharted.terarium.hmiserver.exceptions.HmiResponseExceptionMapper;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;

@Path("/api/extract")
@Slf4j
@Produces(MediaType.APPLICATION_JSON)
@RegisterProvider(HmiResponseExceptionMapper.class)
public class ExtractionResource {

	@Inject
	@RestClient
	ExtractionServiceProxy extractionProxy;

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Map<String, Object> proxyPostMathMLToAMR(List<String> payload, @QueryParam("model") String model) {
			return extractionProxy.postMathMLToAMR(payload, model);
	}

	// checks a taskId status
	@GET
	@Path("/task-result/{taskId}")
	public Response getTaskStatus(
			@PathParam("taskId") final String taskId) {
		return extractionProxy.getTaskStatus(taskId);
	}
}
