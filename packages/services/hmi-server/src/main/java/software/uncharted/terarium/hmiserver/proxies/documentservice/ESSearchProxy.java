package software.uncharted.terarium.hmiserver.proxies.documentservice;

import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import software.uncharted.terarium.hmiserver.annotations.LogRestClientTime;
import software.uncharted.terarium.hmiserver.exceptions.HmiResponseExceptionMapper;

import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * This is a temporary proxy as currently the ES endpoint is just on xDD's development
 * server. Eventually we will want to move this to DocumentProxy
 */
@RegisterRestClient(configKey="xdd-dev-service")
@Produces(MediaType.APPLICATION_JSON)
@RegisterProvider(HmiResponseExceptionMapper.class)
@Path("/api")

public interface ESSearchProxy {

	/**
	 * Perform an elastic search query to xDD
	 * @param apiKey header API key
	 * @param data json query
	 * @return query results
	 */
	@POST
	@Path("/es")
	@LogRestClientTime
	Response search(@HeaderParam("x-api-key") final String apiKey, final String data);

}
